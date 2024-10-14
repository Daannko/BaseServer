Basic server containing variety of features(currently in development)

To run server just use script run.bat in project folder.


KANBAN: https://dankoz.atlassian.net/jira/software/projects/BS/boards/1

PRE-REQUESITS:
   in the folder src/main/resources/certs you need:
   - private.pem - private key for jwt encoder
   - public.pem - public key for jwt decoder
   - Comands to create keys:
     - openssl genrsa -out keypair.pem 2048
     - openssl rsa -in keypair.pem -pubout -out public.pem
     - openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in keypair.pem -out private.pem

   
