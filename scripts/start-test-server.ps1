Write-Host "Starting test server..."
Push-Location "../ParlaGateway"

$jarPath = "target/ParlaGateway-1.0-SNAPSHOT.jar"
$libPath = "lib/*"
$className = "com.parlAquatics.exchange.QuickFIXServer"


Start-Process "java" `
  -ArgumentList "-cp", "$jarPath;$libPath", $className `
  -WindowStyle Normal

Pop-Location

