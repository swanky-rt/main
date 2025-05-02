param (
    [string]$StartRange,
    [string]$EndRange
)

$env:PGPASSWORD = "5DC6QF6e61MeDCa6jZly"

$database = "my test database"
$user = "postgres"
$dbHost = "localhost"
$port = "5432"

$csvPath = "C:\Users\Zelda\CS645\repos\main\app\src\main\java\project_645\shell scripts\postgres_output.csv"

$schemaName = """my test schema"""
$titleBasicsTableName = "title_basics_import"
$titlePrinciplesTableName = "title_principals_import"
$nameBasicsTableName = "name_basics_import"

$movieIdAttribute = "tconst"
$movieTitleAttribute = "primarytitle"
$personIdAttribute = "nconst"
$nameAttribute = "primaryname"

Write-Host "Running query: $Query"

$Query = @"
SELECT $schemaName.$titleBasicsTableName.$movieTitleAttribute,
       $schemaName.$nameBasicsTableName.$nameAttribute
FROM $schemaName.$titleBasicsTableName
JOIN $schemaName.$titlePrinciplesTableName
  ON $schemaName.$titleBasicsTableName.$movieIdAttribute = $schemaName.$titlePrinciplesTableName.$movieIdAttribute
JOIN $schemaName.$nameBasicsTableName
  ON $schemaName.$titlePrinciplesTableName.$personIdAttribute = $schemaName.$nameBasicsTableName.$personIdAttribute
WHERE $movieTitleAttribute >= '$StartRange' AND $movieTitleAttribute <= '$EndRange'
  AND category = 'director'
"@

$escapedQuery = $Query -replace '"', '""'

psql -v client_encoding=UTF8 -d "$database" -h "$dbHost" -p "$port" -U "$user" -c "\copy ($escapedQuery) TO '$csvPath' WITH CSV HEADER ENCODING 'UTF8'"




