# ServerIPSender Plugin

This is a simple Paper plugin that stores the server's IP address and port into
a file and sends the information to a plugin named `VelocityAPI` via plugin
messaging. The data is written to `plugins/ServerIPSender/server-ip.txt` when
the plugin is enabled. If configured with Pterodactyl API credentials, the
plugin will query the panel for the server's default allocation and use that
address instead of the local server configuration.

## Building

Run `mvn package` to compile the plugin. The built JAR will be created in
the `target` directory.

## Configuration

When the plugin first runs it creates a `config.yml` file. To obtain the server
address from a Pterodactyl panel, fill in values for `pterodactyl-url`,
`auth-token` and `server-id`:

```
pterodactyl-url: "https://your-panel.example.com"
auth-token: "<API token>"
server-id: <server id>
```

With these options set the plugin will query the panel for the default
allocation and use that address when saving and sending the server IP.
