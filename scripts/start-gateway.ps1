Write-Host "Starting gateway..."
Start-Process "java" `
  -ArgumentList "-cp ../ParlaGateway/target/ParlaMapper-1.0-SNAPSHOT.jar:lib/* com.parlAquatics.gateway.ParlaGateway"`
  -NoNewWindow -Wait
