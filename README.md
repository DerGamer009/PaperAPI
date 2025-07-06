# ServerIPSender Plugin

This is a simple Paper plugin that stores the server's IP address and port into
a file and sends the information to a plugin named `VelocityAPI` via plugin
messaging. The data is written to `plugins/ServerIPSender/server-ip.txt` when
the plugin is enabled.

## Building

Run `mvn package` to compile the plugin. The built JAR will be created in
the `target` directory.
