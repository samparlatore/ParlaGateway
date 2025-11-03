Write-Host "Starting test server..."
Start-Process "java" `
  -ArgumentList "-cp ./ParlaGateway/target/ParlaGateway-1.0-SNAPSHOT.jar;./ParlaGateway/lib/*" com.parlAquatics.exchange.QuickFIXServer" `
  -NoNewWindow -Wait