# Broadcast-Transaction
Android app to broadcast cryptocurrency transactions from a base43/hex QR code or string.

It currently supports BTC, LTC, DASH, ZCASH and DOGECOIN and their test networks.

It uses the chain.so API to broadcast the transaction.

All feedback is appreciated - JJandJ@tuta.io

This app takes an input from a QR code or text. The input should be a signed transaction in base43 or hex.

The QR code and base43/hex string are generated by a wallet when you create a new transaction. A wallet can also (and usually does) broadcast the transaction to the network.

In cold storage situations, you can create and sign your transaction, but you are unable to broadcast it (as you are offline).

This app allows you to scan the QR code or type (paste?) the base43/hex string and then broadcasts it to the network.

Installation
1) Download the APK file here - https://github.com/JJandJ/Broadcast-Transaction/tree/master/app/release
2) Copy the file to your device, or download using your device.
3) Open the file on your device, this will install the app (you may get a warning about installing apps from unknown sources)
