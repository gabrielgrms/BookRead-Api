# System Architecture Diagram

## Component Interaction Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                         DataManager                              │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │ • Manages providers (s3, gcp, n3)                         │  │
│  │ • Routes operations to appropriate provider               │  │
│  │ • Handles async execution via exec()                      │  │
│  │ • Tracks running operations                               │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                            │
                            │ delegates to
                            ▼
        ┌──────────────────────────────────────────┐
        │                                          │
        ▼                    ▼                     ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│ S3Provider2   │   │ GCPProvider2  │   │ N3Provider2   │
│ (CREATE)      │   │ (UPDATE)      │   │ (DELETE)      │
├───────────────┤   ├───────────────┤   ├───────────────┤
│ • Validates   │   │ • Validates   │   │ • Validates   │
│   new data    │   │   updates     │   │   deletion    │
│ • Checks for  │   │ • Merges data │   │ • Removes     │
│   duplicates  │   │ • Updates     │   │   record      │
│ • Registers   │   │   existing    │   │               │
└───────────────┘   └───────────────┘   └───────────────┘
        │                    │                     │
        │ uses               │ uses                │ uses
        └────────────────────┼─────────────────────┘
                            ▼
                   ┌─────────────────┐
                   │  DataValidator  │
                   ├─────────────────┤
                   │ • Validates IDs │
                   │ • Validates     │
                   │   names         │
                   │ • Formats phone │
                   │ • Formats money │
                   └─────────────────┘
                            │
        ┌───────────────────┴───────────────────┐
        │                                       │
        ▼                                       ▼
┌─────────────────┐                   ┌─────────────────┐
│   DataMemory    │                   │  File Lock      │
├─────────────────┤                   │  System         │
│ private $data   │◄──────────────────┤                 │
│ = []            │   synchronizes    │ • Prevents race │
│                 │                   │   conditions    │
│ • registerData()│                   │ • Thread safety │
│ • updateData()  │                   │   via flock()   │
│ • deleteData()  │                   └─────────────────┘
│ • getAllData()  │
└─────────────────┘
```

## Synchronous Operation Flow

```
User Request
    │
    ├─→ DataManager::executeOperation(provider, data, async=false)
    │
    ├─→ Provider::execute(data)
    │   │
    │   ├─→ DataValidator::validateAndFormat(data)
    │   │   │
    │   │   └─→ Returns validated data
    │   │
    │   └─→ DataMemory::registerData() / updateData() / deleteData()
    │       │
    │       ├─→ acquireLock()
    │       ├─→ Modify $data array
    │       └─→ releaseLock()
    │
    └─→ Return result to user
```

## Asynchronous Operation Flow

```
User Request
    │
    ├─→ DataManager::executeOperation(provider, data, async=true)
    │
    ├─→ DataManager::executeAsync(provider, data)
    │   │
    │   ├─→ Generate unique operation ID
    │   │
    │   ├─→ Write operation data to temp file
    │   │   /tmp/operation_{id}.json
    │   │
    │   ├─→ exec() spawns background PHP process
    │   │   │
    │   │   └─→ New PHP process:
    │   │       │
    │   │       ├─→ new DataManager() ⚠️ NEW INSTANCE
    │   │       │
    │   │       ├─→ executeOperation(..., async=false)
    │   │       │
    │   │       └─→ Write result back to temp file
    │   │
    │   └─→ Return operation ID to user
    │
    └─→ User can check status with getOperationStatus(operationId)
        │
        └─→ Read temp file for result
```

## Data Flow: Create Operation (S3Provider2)

```
┌─────────────────────────────────────────────────────────────┐
│ Input: JSON string                                          │
│ {"id": "001", "name": "João", "lastName": "Silva", ...}    │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│ 1. json_decode($data) - Parse JSON                         │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. Check for duplicate ID                                   │
│    ❌ BUG: isset($memory[$data['id']])                      │
│    ✅ FIX: isset($this->memory->getAllData()[$data['id']])  │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. DataValidator::validateAndFormat($data)                  │
│    • Validate ID exists                                     │
│    • Validate name format (letters only)                    │
│    • Validate lastName format                               │
│    • Format telephone: (XX) XXXXX-XXXX                      │
│    • Round bankAccount to 2 decimals                        │
│    • Round salary to 2 decimals                             │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│ 4. DataMemory::registerData($validatedData)                 │
│    • acquireLock() - Get exclusive lock                     │
│    • $this->data[$id] = $data - Store in array             │
│    • releaseLock() - Release lock                           │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│ Output: Success response                                    │
│ ['status' => 'success', 'operation' => 'create', ...]       │
└─────────────────────────────────────────────────────────────┘
```

## The Bug Location

```
S3Provider2::execute()
{
    $data = (array)json_decode($data);
    $validator = new DataValidator();
    
    ┌──────────────────────────────────────┐
    │ ❌ BUGGY CODE                        │
    │                                      │
    │ if (isset($memory[$data['id']])) {   │
    │     ^^^^^^^^^^^                      │
    │     Undefined variable!              │
    │     Should be:                       │
    │     $this->memory->getAllData()      │
    │ }                                    │
    └──────────────────────────────────────┘
    
    $validatedData = $validator->validateAndFormat($data);
    $this->memory->registerData($validatedData);  // ✅ This part is correct
    return ['status' => 'success', ...];
}
```

## Thread Safety Mechanism

```
DataMemory uses file-based locking:

Process A                    Lock File              Process B
    │                            │                      │
    ├─→ acquireLock()            │                      │
    │   └─→ fopen()              │                      │
    │       flock(LOCK_EX) ───→ [LOCKED]               │
    │                            │                      │
    ├─→ Modify $data             │                      ├─→ acquireLock()
    │                            │                      │   └─→ fopen()
    │                            │                      │       flock(LOCK_EX)
    │                            │                      │       [WAITING...]
    │                            │                      │
    ├─→ releaseLock()            │                      │
    │   └─→ flock(LOCK_UN) ───→ [UNLOCKED]            │
    │                            │                      │
    │                            │   ◄───────────────── └─→ [ACQUIRED LOCK]
    │                            │                          └─→ Modify $data
    │                            │                              └─→ releaseLock()
```

## Key Insights

1. **In-Memory Storage**: Data is stored in a PHP array (`$data[]`), which is NOT persistent across different PHP processes
2. **Async Problem**: Each async operation spawns a NEW PHP process with EMPTY memory
3. **Lock Mechanism**: File-based locking prevents concurrent access issues within the same process
4. **No Shared State**: Different PHP processes cannot share the in-memory array

## Why Bug #1 Prevents Saving

The undefined `$memory` variable causes:
- PHP Notice/Warning (may stop execution depending on error_reporting)
- Duplicate check never works (`isset()` on undefined variable always returns false, so duplicates are never detected)
- In strict mode, causes fatal error before reaching `registerData()`
- Code may appear to work but with broken validation, allowing duplicate IDs
