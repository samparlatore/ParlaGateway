Write-Host "Starting gateway..."
Push-Location "../ParlaGateway"

$jarPath = "target/ParlaGateway-1.0-SNAPSHOT.jar"
$libPath = "lib/*"
$className = "com.parlAquatics.gateway.ParlaGateway"


Start-Process "java" `
  -ArgumentList "-cp", "$jarPath;$libPath", $className `
  -NoNewWindow -Wait

  Pop-Location
