Write-Host "Starting test server..."

$jarPath = "ParlaGateway/target/ParlaGateway-1.0-SNAPSHOT.jar"
$libPath = "ParlaGateway/lib/*"
$className = "com.parlAquatics.exchange.QuickFIXServer"


Start-Process "java" `
  -ArgumentList "-cp", "$jarPath;$libPath", $className `
  -NoNewWindow -Wait
