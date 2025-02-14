It seems you've configured the sync-server correctly; to check if it's running, browse to this endpoint or refresh—if the page loads again, the server is up; ensure the connected database is also running.

This server is meant to communicate only with the **[Linkora](https://github.com/LinkoraApp/Linkora)** apps, available on **Android** and **Desktop**. There's no web-based frontend yet, so you can’t control anything through a web interface. That said, if you’re willing to build your own custom solution from scratch that connects to this server and does what it needs to, then it’s totally possible.

## Important Notes

#{PLACEHOLDER_1}

### **HTTPS Support**
- If you're **hosting locally**, you **can't connect** via **HTTPS** right now.
- HTTPS requires a **trusted certificate** from a recognized Certificate Authority (CA), which isn’t typically applicable for local setups.
- The other way is to manually generate a self-signed certificate and configure it, which may be added in future versions.
- If you're **hosting on a cloud service**, you can use HTTPS by setting up an SSL certificate through your provider.

### **Database & Data Syncing**
- Apps have their **own databases**.
- This server is **only meant to store your data** in the local system that you're currently running on.
- If, for some reason, data gets deleted (maybe uninstalled or whatever), you can **connect to this server**, and the app will **pull everything** from the database that’s currently on your machine to its local database.
- **i.e.,**
    - The app has its **own database**.
    - This server is now **connected to a database**.
    - Linkora **app and server sync data** between the app and the database.

### **Deletion Behavior**
- If something gets **deleted from the Linkora app(s)** while you have a server saved on clients, even if the server is **not up**, it will be **deleted as soon as the server is up** and you open the Linkora app.
- **It's gone, that’s it. You can't undo this.**
- (*I should probably add a trash mechanism instead of deleting permanently, but for now, this can't be undone.*)

### **LWW (Last-Write-Wins) Implementation**
- This server follows **LWW (Last-Write-Wins)**.
- Even if your clients have a saved connection, if the **server is offline**, the **most recently edited data** (from any client) will be updated in the database on your machine once the server is back online and the client apps reconnect.

### **Blank Pages on Other Routes**
- If you're seeing blank pages for other routes, that's completely normal since there's no UI with this server except for this page.
- Except for this route, every other route is secured by an auth token. Access is only granted if the client provides the correct auth token that you’ve set. Without it, they won’t be able to access anything.

## Troubleshooting & Additional Help
- Always go through the [README](https://github.com/LinkoraApp/sync-server/blob/master/README.md); it covers most of the info you're probably looking for.
- If something isn’t covered here, go through **[GitHub issues](https://github.com/LinkoraApp/sync-server/issues)**; you might find the solution there.
- If not, **[create an issue on GitHub](https://github.com/LinkoraApp/sync-server/issues/new)**, and I'll fix it when I get some time.
- You can also [join the Discord](https://discord.gg/ZDBXNtv8MD) if you want. It’s there for questions, updates, or just to hang out.

---
#### Workflow of Linkora, which should make it easier to understand how everything works:

<a href="https://github.com/user-attachments/assets/bb2d9b7e-92c4-41ed-82d3-ad821cc65638" onclick="window.open(this.href, '_blank'); return false;">
  <img alt="linkora-outline.png" src="https://github.com/user-attachments/assets/bb2d9b7e-92c4-41ed-82d3-ad821cc65638" style="max-width: 100%; height: auto;">
</a>
