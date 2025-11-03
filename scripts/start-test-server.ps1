Write-Host "Starting test server..."
Start-Process "java" `
  -ArgumentList "-cp ../ParlaGateway/target/ParlaMapper-1.0-SNAPSHOT.jar;lib/* com.parlAquatics.gateway.simulation.quickFIXServer.QuickFIXServer" `
  -NoNewWindow -Wait