<?php
require_once 'config.php';

echo json_encode([
    'success' => true,
    'message' => 'Database connected successfully!',
    'connection_type' => get_class($conn)
]);

$conn->close();
?>