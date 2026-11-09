<?php

require_once __DIR__ . '/../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    exit;
}

$email = $_POST['email'] ?? '';
$connection = databaseConnection();
$check = $connection->prepare(
    'SELECT email FROM textletsretrofit WHERE email = :email'
);
$check->execute(['email' => $email]);

if ($check->fetch()) {
    echo json_encode(['email' => $email], JSON_THROW_ON_ERROR);
    exit;
}

$insert = $connection->prepare(
    'INSERT INTO textletsretrofit (date, name, email, password) ' .
    'VALUES (:date, :name, :email, :password)'
);
$insert->execute([
    'date' => $_POST['date'] ?? '',
    'name' => $_POST['name'] ?? '',
    'email' => $email,
    'password' => $_POST['password'] ?? '',
]);

echo 'null';
