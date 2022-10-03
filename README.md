# socketPortForward
    java -jar socket-0.1.jar -listen 4000 -forward <remote_host>:<remote_port>

Add -debug flag in case you want to print all the content sent/received.

## Example of usage

- Man in the middle.
- Debug what is sent/received from one host:port.
- Bypass network security rules.
