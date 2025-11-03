Write-Host "Starting gateway..."

$jarPath = "ParlaGateway/target/ParlaGateway-1.0-SNAPSHOT.jar"
$libPath = "ParlaGateway/lib/*"
$className = "com.parlAquatics.gateway.ParlaGateway"


Start-Process "java" `
  -ArgumentList "-cp", "$jarPath;$libPath", $className `
  -NoNewWindow -Wait