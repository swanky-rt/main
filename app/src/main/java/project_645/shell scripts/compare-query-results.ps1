param (
    [string]$StartRange,
    [string]$EndRange
)

$env:PGPASSWORD = "PostgreSQL password goes in these quotes"

$database = "Database password goes in these quotes"
$user = "postgres user goes in these quotes"
$dbHost = "host goes in these quotes (likely localhost)"
$port = "port number goes here (likely 5432)"

$csvPath = "Absolute path to CSV (including the filename itself)"

$schemaName = "schema name goes in these quotes"
$titleBasicsTableName = "title basics table name goes in these quotes"
$titlePrinciplesTableName = "title principles table name goes in these quotes"
$nameBasicsTableName = "name basics table name goes in these quotes"

$movieIdAttribute = "attribute name of movie ID (assumed to be the same for both titlebasics, and title principles)"
$movieTitleAttribute = "title attribute name from titlebasics"
$personIdAttribute = "personId attribute name (assumed to be the same across both titlePrimciples, and nameBasics)"
$nameAttribute = "name attribute of nameBasics"

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




