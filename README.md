Linkora Sync-Server is self-hostable and based on a `single-user server` mechanism, meaning it is designed to be used
exclusively by one individual rather than multiple users.

---

### 1. Prerequisites

#### For Local Hosting (Using JAR):

- Ensure **Java 11 or later** is installed on your machine to run the server properly.

#### For Docker Hosting:

- If you are using Docker, no additional setup is required. Docker handles all dependencies.

#### Supported Databases:

- Linkora supports SQL-based databases such as **MySQL**, **SQLite**, **PostgreSQL**, and other databases supported by
  the [Exposed](https://github.com/JetBrains/Exposed?tab=readme-ov-file#supported-databases).
  - Note: The server has been tested with MySQL, SQLite, and PostgreSQL.

---

### 2. Server Configuration

Linkora Sync Server can be configured in two ways:

1. Using the `linkoraConfig.json` file.
2. Using environment variables.

#### 1. Using `linkoraConfig.json`

This file is auto-generated on the first run of the server if no environment variables are detected. During setup, you
will be prompted to provide the required values, which will be saved locally.

`linkoraConfig.json`:

```json
{
  "databaseUrl": "",
  "databaseUser": "",
  "databasePassword": "",
  "hostAddress": "",
  "serverPort": 45454,
  "serverAuthToken": ""
}
```

- `databaseUrl`: The database URL (without username or password).
  - `databaseUrl` should always start with the name of the database you're using:
    - PGSQL (or) PostgreSQL :
      ```
      postgresql://someAddress:port/linkora
      ``` 
    - MySQL :
      ```
      mysql://someAddress:port/linkora
      ``` 
    - SQLite :
      ```
      sqlite:path-to-db
      ``` 
      `path-to-db` should look something like `sqlite:/home/saketh/Documents/sqlite/databaseFileName`
- `databaseUser`: The username of the database you want to connect to.
  - If you are using SQLite, leave this blank or press Enter when the setup prompts for it.
- `databasePassword`: The password of the database you want to connect to.
  - If you are using SQLite, leave this blank or press Enter when the setup prompts for it.
- `hostAddress`: The host address of the server. If not provided, it defaults to the `IPv4` of the network currently connected to. When connecting from Linkora apps, you should pass this address instead of `localhost`.
- `serverPort`: The port number on which the server runs. If not provided, defaults to `45454`.
- `serverAuthToken`: A secure token used to authenticate requests from the Linkora app. Treat this like a password and
  keep it confidential.


**Note**: A pre-written `linkoraConfig.json` file can also be used on remote hosting if no environment variables are
  provided or if you prefer this method. However, ensure the file is correctly placed.

#### 2. Using Environment Variables

When hosting on remote services or using Docker, environment variables are recommended. The following variables must be
set:

- `LINKORA_SERVER_USE_ENV_VAL`: Set this to `true` to enable the use of environment variables. This must always be `true` when using environment variables.
- `LINKORA_DATABASE_URL`: The database URL (without username or password).
- `LINKORA_DATABASE_USER`: The username of the database you want to connect to.
  - If you are using SQLite, leave this blank.
- `LINKORA_DATABASE_PASSWORD`: The password of the database you want to connect to.
  - If you are using SQLite, leave this blank.
- `LINKORA_SERVER_AUTH_TOKEN`: A secure token used to authenticate requests from the Linkora app. Treat this like a
  password and
  keep it confidential.
- `LINKORA_HOST_ADDRESS`: The host address of the server. If not provided, it defaults to the `IPv4` of the network currently connected to. When connecting from Linkora apps, you should pass this address instead of `localhost`.
- `LINKORA_SERVER_PORT`: The port number on which the server runs. If not provided, defaults to `45454`.

### 3. Hosting Options

Linkora Sync Server supports both local and remote hosting. Below are the details for each method:

#### 3.1 Local Hosting

You can host the server locally using either the JAR file or Docker.

##### 3.1.1 Using JAR:

- Use the prebuilt JAR file from the latest release.

**Important**: Instead of double-clicking the JAR file, always open a terminal in the directory where the JAR file is
located and run the following command:

```
java -jar "linkoraSyncServer.jar"
```

This ensures the server runs as expected and avoids potential issues.

- **Recommendation**:
  - Place the server JAR file in a separate folder before running it. This is because the `linkoraConfig.json` file is
    generated in the same directory as the JAR file.
  - For local hosting with JAR, using `linkoraConfig.json` (this file is auto-generated on the first run of the server
    if no environment variables are detected) is recommended.

##### 3.1.2 Using Docker:

Docker Pull Command:

```
docker pull sakethpathike/linkora-sync-server
```

You can set these variables directly in the Docker Desktop GUI before running the image or pass them using the `-e` flag:

```
docker run -e LINKORA_SERVER_USE_ENV_VAL="true" -e LINKORA_DATABASE_URL="mysql://your-db-url" -e LINKORA_DATABASE_USER="your-db-user" -e LINKORA_DATABASE_PASSWORD="your-db-password" -e LINKORA_SERVER_AUTH_TOKEN="your-auth-token" sakethpathike/linkora-sync-server
```

Replace `your-db-url`, `your-db-user`, `your-db-password`, and `your-auth-token` with your actual database and
authentication details.

#### 3.2 Remote Hosting

- For remote hosting, pass the environment variables using the environment variable service provided by your cloud
  hosting provider.
- Ensure `LINKORA_SERVER_USE_ENV_VAL` is set to `true` and all other required variables are configured.

### Key Features

- **Delta-Sync Mechanism**: Linkora uses a delta-sync mechanism to efficiently synchronize data between the server and
  clients.
- **Two-Way Sync**: The server supports two-way synchronization, ensuring that changes on the server are reflected on
  the client and vice versa.
- **Real-Time Updates**: Events on the server are sent to clients via socket, ensuring data is always up-to-date.
- This server implements the LWW (Last-Write-Wins) mechanism, where the latest client update takes effect.

### Important Notes

- **Client-Server Compatibility**: Always verify client-server compatibility to confirm that the applications work
  together properly.
- **Security**: The `serverAuthToken`/`LINKORA_SERVER_AUTH_TOKEN` acts as a password to your server. Never share it
  publicly.
- **Database Compatibility**: While Linkora supports multiple databases, ensure your chosen database is properly
  configured and accessible.
- **Local vs Remote Hosting**:
  - For local hosting with JAR, `linkoraConfig.json` is recommended.
  - For local hosting with Docker, use the provided `docker run` command with environment variables.
  - For remote hosting, use the environment variable service provided by your cloud hosting provider.

### Systemd setup

Create a service file in `/etc/systemd/system` and enable it to run at startup.
Config & JAR files are in the same folder path.
Add the below to your file (change values as per your setup) and name the file as `linkora.service`.

```javascript
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

```javascript
systemctl daemon-reload
systemctl start linkora.service
systemctl enable linkora.service
```

#### Workflow of Linkora, which should make it easier to understand how everything works:

<a href="https://github.com/user-attachments/assets/bb2d9b7e-92c4-41ed-82d3-ad821cc65638" onclick="window.open(this.href, '_blank'); return false;">
  <img alt="linkora-outline.png" src="https://github.com/user-attachments/assets/bb2d9b7e-92c4-41ed-82d3-ad821cc65638" style="max-width: 100%; height: auto;">
</a>

### Join the Community

[![Join us on Discord](https://discord.com/api/guilds/1214971383352664104/widget.png?style=banner2)](https://discord.gg/ZDBXNtv8MD)

### Contribute

Want to help improve Linkora Sync Server? You can contribute by:

- Reporting issues
- Submitting pull requests

### License

This project is licensed under the MIT License.
