# Linkora Sync-Server
A self-hosted sync server designed for individual use and built for single-user setups, keeping your Linkora apps in sync.

## Key Features
- **Delta-Sync**: Only transfers changed data for efficiency
- **Two-Way Sync**: Changes flow in both directions between the server and clients
- **Real-Time Updates**: Live updates via WebSocket connections
- **Last-Write-Wins**: If multiple devices update the same data, the latest write wins the conflict
- **Multi-Database Support**: Works with MySQL, PostgreSQL, SQLite, and other SQL databases supported by [JetBrains Exposed](https://github.com/JetBrains/Exposed)
- **Deployment Options**: Local JAR, Docker containers, or cloud hosting

## Quick Start

### Prerequisites
**For JAR deployment:**
- Java 11 or later

**For Docker deployment:**
- Docker (handles all dependencies, no Java installation required)

**Supported Databases:**
- **Tested**: MySQL, PostgreSQL, SQLite
- **Untested**: Other databases supported by [JetBrains Exposed](https://github.com/JetBrains/Exposed)

**Warning**: Untested databases may have compatibility issues. I recommend sticking to MySQL, PostgreSQL, or SQLite for the best experience.

## Configuration
Choose between configuration file or environment variables:

### Option 1: Configuration File (`linkoraConfig.json`)
Auto-generated on first run - the server will prompt you for required configuration values. The `hostAddress` and `serverPort` fields are automatically set by the server:

```json
{
  "databaseUrl": "mysql://localhost:3306/linkora",
  "databaseUser": "your_username",
  "databasePassword": "your_password",
  "hostAddress": "xxx.xxx.xxx.xxx",
  "serverPort": 45454,
  "serverAuthToken": "your_secure_token"
}
```

### Option 2: Environment Variables
Set these environment variables for containerized deployments, cloud hosting, or any deployment where you prefer environment variables over config files. The `LINKORA_HOST_ADDRESS` and `LINKORA_SERVER_PORT` are optional as the server will automatically detect/set them:

```bash
LINKORA_SERVER_USE_ENV_VAL=true
LINKORA_DATABASE_URL=mysql://localhost:3306/linkora
LINKORA_DATABASE_USER=your_username
LINKORA_DATABASE_PASSWORD=your_password
LINKORA_HOST_ADDRESS=xxx.xxx.xxx.xxx
LINKORA_SERVER_PORT=45454
LINKORA_SERVER_AUTH_TOKEN=your_secure_token
```

### Database URL Formats
| Database | URL Format |
|----------|------------|
| MySQL | `mysql://hostname:port/database_name` |
| PostgreSQL | `postgresql://hostname:port/database_name` |
| SQLite | `sqlite:/path/to/database/file.db` |

**Important**: For SQLite, leave `databaseUser` and `databasePassword` empty.

## Deployment Options

### Local Deployment

#### Using JAR File
1. Download the latest release JAR file
2. Create a dedicated folder for the server
3. **Critical**: Run from terminal (never double-click the JAR):

```bash
java -jar linkoraSyncServer.jar
```

**Recommendation**: Place the JAR file in a separate folder as `linkoraConfig.json` is generated in the same directory.

#### Using Docker
```bash
# Pull the latest version of the image
docker pull sakethpathike/linkora-sync-server:latest

# Run the server with environment variables
# Note: LINKORA_HOST_ADDRESS and LINKORA_SERVER_PORT are optional
docker run -d \
  -e LINKORA_SERVER_USE_ENV_VAL=true \
  -e LINKORA_DATABASE_URL=mysql://your-db-url \
  -e LINKORA_DATABASE_USER=your-username \
  -e LINKORA_DATABASE_PASSWORD=your-password \
  -e LINKORA_HOST_ADDRESS=xxx.xxx.xxx.xxx \
  -e LINKORA_SERVER_PORT=45454 \
  -e LINKORA_SERVER_AUTH_TOKEN=your-secure-token \
  -p 45454:45454 \
  --name linkora-sync-server \
  sakethpathike/linkora-sync-server:latest
```

### Cloud Deployment
For cloud hosting platforms:
1. Set `LINKORA_SERVER_USE_ENV_VAL=true` in your environment variables
2. Configure all required environment variables through your platform's interface
3. Ensure your database is accessible

## Systemd Setup
Create a service file in `/etc/systemd/system` and enable it to run at startup.
Config & JAR files are in the same folder path.
Add the below to your file (change values as per your setup) and name the file as `linkora.service`.

```bash
[Unit]
Description=linkora service
After=network.target

[Service]
SuccessExitStatus=143
User=root
Group=root
Type=simple
EnvironmentFile=PATH_TO_CONFIG/linkoraConfig.json
WorkingDirectory=PATH_TO_JAR_FILE
ExecStart=/usr/bin/java -jar linkoraSyncServer.jar
ExecStop=/bin/kill -15 $MAINPID

[Install]
WantedBy=multi-user.target
```

Once saved, reload the daemon & start the service:
```bash
systemctl daemon-reload
systemctl start linkora.service
systemctl enable linkora.service
```

## Firewall Configuration
If you can't connect to the server, allow the port through your firewall:

```bash
# Allow the port (default: 45454)
sudo firewall-cmd --add-port=45454/tcp --permanent
sudo firewall-cmd --reload

# Verify the port is open
sudo firewall-cmd --list-ports
```

## Critical Security & Connection Notes

**Authentication Token**: Treat `serverAuthToken` as a password—never share it publicly.

**Network Access**: Connect using the server machine's IPv4 address (or the one detected and used by the server) instead of `localhost` when accessing from other devices.

**Client-Server Compatibility**: Always verify client-server version compatibility. Mismatched versions may cause sync failures.

## Troubleshooting

### Common Issues

**Cannot connect to server from Linkora app**
- Check if the server port is allowed through your firewall
- Ensure you're using the server machine's IPv4 address (or the one detected and used by the server) instead of `localhost` when connecting from other devices
- Verify the `serverAuthToken` matches between server and client

**Server fails to start**
- Ensure Java 11+ is installed and accessible
- Check database connectivity and credentials
- Verify the database URL format is correct for your database type
- For JAR deployment, ensure you're running from terminal, not double-clicking

**Database connection errors**
- Confirm your database server is running and accessible
- Verify database URL, username, and password are correct
- For SQLite, ensure the file path is accessible and the directory exists

**Configuration issues**
- Ensure `LINKORA_SERVER_USE_ENV_VAL=true` when using environment variables
- Verify all required fields are properly set in your chosen configuration method

## How It Works
![Linkora Workflow](https://github.com/user-attachments/assets/bb2d9b7e-92c4-41ed-82d3-ad821cc65638)

For detailed technical information about the synchronization mechanism, read the [technical blog post](https://sakethpathike.github.io/blog/synchronization-in-linkora).

## Important Notes
- **Single User Design**: Designed exclusively for individual use, not multi-user environments
- **Configuration File Location**: For JAR deployments, `linkoraConfig.json` is created in the same directory as the JAR file when you first run the server
- **Database Compatibility**: Linkora supports multiple databases through Exposed. The server has been tested with MySQL, SQLite, and PostgreSQL
- **Environment Variables**: For local JAR hosting, `linkoraConfig.json` is recommended. For Docker and cloud hosting, use environment variables
- **Host Address**: The `hostAddress` field automatically defaults to the machine's current network IPv4 address. The server detects this automatically, so no manual configuration is needed. When connecting from Linkora apps, use this address instead of `localhost`

## Support
**Star the repo** if you find Linkora useful

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/sakethpathike)

## Community
[![Join us on Discord](https://discord.com/api/guilds/1214971383352664104/widget.png?style=banner2)](https://discord.gg/ZDBXNtv8MD)

## Contributing
Contributions are welcome! You can help by:
- Reporting bugs and issues
- Suggesting new features
- Submitting pull requests
- Improving documentation

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.