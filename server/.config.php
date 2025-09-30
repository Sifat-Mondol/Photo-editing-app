
<?php
// Database configuration
$host = 'localhost';
$username = 'root';
$password = '';
$database = 'photo_editor_app';

// Create mysqli connection
$conn = new mysqli($host, $username, $password, $database);

// Check connection
if ($conn->connect_error) {
    die(json_encode([
        'success' => false, 
        'message' => 'Connection failed: ' . $conn->connect_error
    ]));
}

// Set charset to utf8
$conn->set_charset("utf8");
?>
