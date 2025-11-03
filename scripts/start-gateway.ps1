Write-Host "Starting gateway..."
Start-Process "java" `
  -ArgumentList "-cp ./ParlaGateway/target/ParlaGateway-1.0-SNAPSHOT.jar:lib/* com.parlAquatics.gateway.ParlaGateway"`
  -NoNewWindow -Wait
