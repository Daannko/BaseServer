



PRE-REQUESITS:

in the folder src/main/resources/certs you need:
 - private.pem - private key for jwt encoder
 - public.pem - public key for jwt decoder
 - Comands to create keys:
   - openssl genrsa -out keypair.pem 2048
   - openssl rsa -in keypair.pem -pubout -out public.pem
   - openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in keypair.pem -out private.pem

