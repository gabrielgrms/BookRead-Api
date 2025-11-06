<?php

/**
 * CORRECTED VERSION OF THE PHP CODE
 * 
 * This file contains the fixed version of the DataManager system
 * with all bugs corrected.
 */

class DataManager {
    private $providers = [];
    private $memory;
    private $runningOperations = [];
    
    public function __construct() {
        $this->memory = new DataMemory();
        $this->providers = [
            's3' => new S3Provider2($this->memory),
            'gcp' => new GCPProvider2($this->memory),
            'n3' => new N3Provider2($this->memory)
        ];
    }
    
    public function executeOperation($provider, $data, $async = true) {
        if (!isset($this->providers[$provider])) {
            return ['status' => 'error', 'message' => 'Unknown provider'];
        }
        
        if ($async) {
            return $this->executeAsync($provider, $data);
        } else {
            return $this->providers[$provider]->execute($data);
        }
    }
    
    private function executeAsync($provider, $data) {
        $operationId = uniqid();
        
        // Simulate async operation using output buffering and background processing
        $tempFile = sys_get_temp_dir() . "/operation_{$operationId}.json";
        
        // Store operation data
        file_put_contents($tempFile, json_encode([
            'provider' => $provider,
            'data' => $data,
            'status' => 'running',
            'started_at' => time()
        ]));
        
        // Execute in background using exec (PHP's way to simulate async without pcntl)
        // FIX: Changed ConcurrentOperationManager to DataManager
        // NOTE: This approach is fragile. For production, use proper message queues (RabbitMQ, Redis Queue)
        $cmd = sprintf(
            'php -r "
            include_once \'%s\';
            \$manager = new DataManager();
            \$data = json_decode(file_get_contents(\'%s\'), true);
            \$result = \$manager->executeOperation(\$data[\'provider\'], \$data[\'data\'], false);
            \$data[\'result\'] = \$result;
            \$data[\'status\'] = \'completed\';
            \$data[\'completed_at\'] = time();
            file_put_contents(\'%s\', json_encode(\$data));
            " > /dev/null 2>&1 &',
            __FILE__,
            $tempFile,
            $tempFile
        );
        
        exec($cmd);
        
        $this->runningOperations[$operationId] = $tempFile;
        
        return [
            'status' => 'queued',
            'operation_id' => $operationId,
            'message' => 'Operation started asynchronously'
        ];
    }
    
    public function getOperationStatus($operationId) {
        if (!isset($this->runningOperations[$operationId])) {
            return ['status' => 'error', 'message' => 'Operation not found'];
        }
        
        $tempFile = $this->runningOperations[$operationId];
        
        if (!file_exists($tempFile)) {
            return ['status' => 'error', 'message' => 'Operation file not found'];
        }
        
        $operationData = json_decode(file_get_contents($tempFile), true);
        
        if ($operationData['status'] === 'completed') {
            // Clean up temp file
            unlink($tempFile);
            unset($this->runningOperations[$operationId]);
        }
        
        return $operationData;
    }
    
    public function generateReport($provider) {
        if (!isset($this->providers[$provider])) {
            throw new InvalidArgumentException("Unknown provider: $provider");
        }
        
        return $this->providers[$provider]->generateReport();
    }
    
    public function getAllData() {
        return $this->memory->getAllData();
    }
}

abstract class BaseProvider {
    protected $memory;
    
    public function __construct(DataMemory $memory) {
        $this->memory = $memory;
    }
    
    abstract public function execute($data);
    
    public function generateReport() {
        $reportData = "id,name,lastName,telephone,bankAccount,salary,balance\n";
        $memory = $this->memory->getAllData();
        foreach ($memory as $data) {
            $balance = round(floatval($data['bankAccount'] + 
            ($data['salary'] * $this->getCurrencyConversionRate('USD', 'BRL') * 40)), 2);
            
            $reportData .= sprintf(
                "%s,%s,%s,%s,%.2f,%.2f,%.2f\n",
                $data['id'],
                $data['name'],
                $data['lastName'],
                $data['telephone'],
                $data['bankAccount'],
                $data['salary'],
                $balance
            );
        }
        file_put_contents('report.csv', $reportData);
    }

    protected function formatCurrency($value) {
        return round($value, 2);
    }

    protected function getCurrencyConversionRate($from, $to) {
        $url = "https://api.exchangerate-api.com/v4/latest/{$from}";
        
        $context = stream_context_create([
            'http' => [
                'timeout' => 10,
                'method' => 'GET'
            ]
        ]);
        
        $response = @file_get_contents($url, false, $context);
        
        if ($response === false) {
            // Fallback rate if API fails
            return ($from === 'USD' && $to === 'BRL') ? 5.20 : 0.19;
        }
        
        $data = json_decode($response, true);
        return isset($data['rates'][$to]) ? $data['rates'][$to] : 5.20;
    }
}

class S3Provider2 extends BaseProvider {
    public function execute($data) {
        try {
            $data = (array)json_decode($data);
            $validator = new DataValidator();
            
            // FIX: Properly check if ID already exists in memory
            $existingData = $this->memory->getAllData();
            if (isset($existingData[$data['id']])) {
                throw new InvalidArgumentException("There is already a data with this ID");
            }
            
            $validatedData = $validator->validateAndFormat($data);
            $this->memory->registerData($validatedData);
            return ['status' => 'success', 'operation' => 'create', 'id' => $data['id']];
        } catch(Exception $e) {
            return ['status' => 'error', 'message' => $e->getMessage()];
        }
    }
}

class GCPProvider2 extends BaseProvider {
    public function execute($data) {
        try {
            $data = (array)json_decode($data);
            $memory_data = $this->memory->getAllData()[$data['id']];
            $validator = new DataValidator();
            $updatedData = $validator->validateAndFormat(array_merge($memory_data, $data));
            $this->memory->updateData($updatedData);
            return ['status' => 'success', 'operation' => 'update', 'id' => $data['id']];
        } catch (Exception $e) {
            return ['status' => 'error', 'message' => $e->getMessage()];
        }
    }

    public function generateReport() {
        // FIX: Initialize $reportData as string, not array
        $reportData = "id,name,lastName,telephone,bankAccount,salary,balance\n";
        $memory = $this->memory->getAllData();
        foreach ($memory as $data) {
            $balance = ($data['bankAccount'] * $this->getCurrencyConversionRate('BRL', 'USD')) + $data['salary'] * 40;
            
            $reportData .= sprintf(
                "%s,%s,%s,%s,%.2f,%.2f,%.2f\n",
                $data['id'],
                $data['name'],
                $data['lastName'],
                $data['telephone'],
                $data['bankAccount'],
                $data['salary'],
                $balance
            );
        }
        file_put_contents('report.csv', $reportData);
    }
}

class N3Provider2 extends BaseProvider {
    public function execute($data) {
        try {
            $data = (array)json_decode($data);
            if (!isset($data['id']) || empty($data['id'])) {
                throw new InvalidArgumentException("ID is required for deletion");
            }

            $deleted = $this->memory->deleteData($data['id']);
            if ($deleted) {
                return ['status' => 'success', 'operation' => 'delete', 'id' => $data['id']];
            }
            return ['status' => 'error', 'message' => 'Record not found'];
        } catch(Exception $e) {
            return ['status' => 'error', 'message' => $e->getMessage()];
        }
    }
}

class DataValidator {
    public function validateAndFormat($data) {
        $formatted = [];
        
        // Validate ID
        if (!isset($data['id']) || empty($data['id'])) {
            throw new InvalidArgumentException("ID is required");
        }
        $formatted['id'] = $data['id'];
        
        // Validate and format name
        if (!isset($data['name']) || !$this->validateName($data['name'])) {
            throw new InvalidArgumentException("Invalid name format");
        }
        $formatted['name'] = trim($data['name']);
        
        // Validate and format lastName
        if (!isset($data['lastName']) || !$this->validateName($data['lastName'])) {
            throw new InvalidArgumentException("Invalid lastName format");
        }
        $formatted['lastName'] = trim($data['lastName']);
        
        // Format telephone
        if (!$formatted['telephone'] = $this->formatTelephone($data['telephone'])) {
            throw new InvalidArgumentException("Invalid phone number");
        }

        // Format bankAccount (BRL)
        $formatted['bankAccount'] = round(floatval($data['bankAccount']), 2);
        
        // Format salary (USD)
        $formatted['salary'] = round(floatval($data['salary']), 2);
        
        return $formatted;
    }

    private function validateName($name) {
        return preg_match('/^[a-zA-ZÀ-ÿ\sçÇãÃõÕáÁéÉíÍóÓúÚ]+$/', $name);
    }

    private function formatTelephone($telephone) {
        $phone = preg_replace('/\D/', '', $telephone);
        if (strlen($phone) != 11) {
            return false;
        }
        return '(' . substr($phone, 0, 2) . ') ' . 
                substr($phone, 2, 5) . '-' .
                substr($phone, 7);
    }
}   

class DataMemory {
    private $data = [];
    private $lockFile;
    
    public function __construct() {
        $this->lockFile = sys_get_temp_dir() . '/data_memory.lock';
    }
    
    private function acquireLock() {
        $handle = fopen($this->lockFile, 'w');
        if (!$handle) {
            throw new Exception("Could not create lock file");
        }
        
        if (!flock($handle, LOCK_EX)) {
            fclose($handle);
            throw new Exception("Could not acquire lock");
        }
        
        return $handle;
    }
    
    private function releaseLock($handle) {
        flock($handle, LOCK_UN);
        fclose($handle);
    }
    
    public function registerData($data) {
        $handle = $this->acquireLock();
        try {
            $this->data[$data['id']] = $data;
        } finally {
            // FIX: Use positional parameter instead of named parameter
            $this->releaseLock($handle);
        }
    }
    
    public function updateData($data) {
        $handle = $this->acquireLock();
        try {
            $id = $data['id'];
            if (isset($this->data[$id])) {
                $this->data[$id] = $data;
                return true;
            }
            throw new InvalidArgumentException("There is no data with this ID in memory");
        } finally {
            $this->releaseLock($handle);
        }
    }
    
    public function deleteData($id) {
        $handle = $this->acquireLock();
        try {
            if (isset($this->data[$id])) {
                unset($this->data[$id]);
                return true;
            }
            return false;
        } finally {
            $this->releaseLock($handle);
        }
    }
    
    public function getAllData() {
        $handle = $this->acquireLock();
        try {
            return $this->data;
        } finally {
            $this->releaseLock($handle);
        }
    }
}

// Example usage:
if (basename(__FILE__) == basename($_SERVER['PHP_SELF'])) {
    echo "=== DataManager Test ===\n\n";
    
    $manager = new DataManager();
    
    // Test data
    $testData = json_encode([
        'id' => '001',
        'name' => 'João',
        'lastName' => 'Silva',
        'telephone' => '11987654321',
        'bankAccount' => 1000.50,
        'salary' => 5000.00
    ]);
    
    // Test synchronous create
    echo "Testing CREATE operation (synchronous):\n";
    $result = $manager->executeOperation('s3', $testData, false);
    print_r($result);
    echo "\n";
    
    // Verify data was saved
    echo "Data in memory:\n";
    print_r($manager->getAllData());
    echo "\n";
    
    // Test duplicate ID
    echo "Testing duplicate ID:\n";
    $result = $manager->executeOperation('s3', $testData, false);
    print_r($result);
    echo "\n";
    
    // Test update
    echo "Testing UPDATE operation:\n";
    $updateData = json_encode([
        'id' => '001',
        'salary' => 6000.00
    ]);
    $result = $manager->executeOperation('gcp', $updateData, false);
    print_r($result);
    echo "\n";
    
    // Verify update
    echo "Data after update:\n";
    print_r($manager->getAllData());
    echo "\n";
    
    // Test delete
    echo "Testing DELETE operation:\n";
    $deleteData = json_encode(['id' => '001']);
    $result = $manager->executeOperation('n3', $deleteData, false);
    print_r($result);
    echo "\n";
    
    // Verify deletion
    echo "Data after delete:\n";
    print_r($manager->getAllData());
    echo "\n";
}
