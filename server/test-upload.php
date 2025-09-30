<?php
require_once 'config.php';

// Get your actual user ID first
$result = $conn->query("SELECT id FROM users LIMIT 1");
if ($result->num_rows === 0) {
    die("ERROR: No users found in database");
}
$user = $result->fetch_assoc();
$user_id = $user['id'];

echo "Using User ID: " . $user_id . "<br>";

// Test data
$image_url = "http://192.168.121.100/BcsBangla/uploads/test.jpg";
$description = "Test photo";

$stmt = $conn->prepare("INSERT INTO photos (user_id, image_url, description, created_at) VALUES (?, ?, ?, NOW())");
$stmt->bind_param("iss", $user_id, $image_url, $description);

if ($stmt->execute()) {
    echo "SUCCESS! Photo ID: " . $stmt->insert_id . "<br>";
    echo "Photo inserted into database!";
} else {
    echo "ERROR: " . $stmt->error;
}

$stmt->close();
$conn->close();
?>