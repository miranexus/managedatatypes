<?php

require_once __DIR__ . '/../config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    exit;
}

$statement = databaseConnection()->query(
    'SELECT date, name, email, password FROM textletsretrofit'
);
echo json_encode($statement->fetchAll(), JSON_THROW_ON_ERROR);
