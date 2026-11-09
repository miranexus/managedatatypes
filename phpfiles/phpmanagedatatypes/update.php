<?php

require_once __DIR__ . '/../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    exit;
}

$statement = databaseConnection()->prepare(
    'UPDATE textletsretrofit SET name = :name, password = :password ' .
    'WHERE email = :email'
);
$statement->execute([
    'name' => $_POST['name'] ?? '',
    'password' => $_POST['password'] ?? '',
    'email' => $_POST['email'] ?? '',
]);
http_response_code(204);
