# WPF-VS - TSP mit ACO
## Traveling Salesman Problem mit Ant Colony Optimization

Implementierung einer Anwendung für verteilte Systeme zur Lastenverteilung des Traveling Salesman Problem mit Ant Colony Optimization.

## Technologien

Front-End

- HTML 5
- CSS 3
- [jQuery 1.12](https://code.jquery.com/jquery-1.12.4.min.js)
- [jQuery-UI](http://jqueryui.com/download/)
- [vis.js](http://visjs.org/#download_install)
- [Socket.io](https://socket.io/)

Back-End

- [Java 8](https://java.com/de/download/)
- [MPJ Express](http://mpj-express.org/)
- [Node.js 7.1](https://nodejs.org/en/)
- [Express.js](http://expressjs.com/de/)
- [MongoDB 3.4](https://www.mongodb.com/download-center?jmp=nav#community)

## Ordnetstruktur

Das Projekt wurde zum Großteil mit der IDE [Eclipse](https://www.eclipse.org/downloads/?) entwickelt und weist eine entsprechende Ordnerstruktur vor. Der Node.js Anteil wurde mit der IDE [Visual Studio Code](https://code.visualstudio.com/) umgesetzt.

- doc - enthält Java Referenzdokumentation als ZIP-Archiv
- ext - enthält Java Erweiterungen, bspw. für JSON Bearbeitung
- json_output - enthält JSON Beispielausgaben der JAR Anwendung
- server - enthält die gesamte Node.js Applikation
   - cfg - enthält JSON Konfigurationsdateien für die JAR Anwendung
   - client - enthält alle Ressourcen (index.html etc.) für den Client
   - mpj_tspwithaco.jar - entspricht kompilierten JAR Anwendung aus dem src Ordner
- src - enthält den Java Quellcode zur Bearbeitung der JAR Anwendung
- tsp_jobs_example - enthält TSP-Job Beispiel JSON Dokumente für das Multi-TSP Szenario

## Ausführung mit MPJ Express multicore Konfiguration und GUI

- klonen des GitHub Projekts (vorzugsweise in Eclipse, sofern Java angepasst werden sollte)
- starten einer MongoDB Instanz mit eingerichtetem Zugang (DB: admin, Benutzer: admin, Passwort: secret)
- starten der Node.js Applikation über app.js im Ordner server
- aufruf der Applikation in einem Browser per localhost:3000

## Ausführung mit MPJ Express cluster Konfiguration ohne GUI

- klonen des GitHub Projekts (vorzugsweise in Eclipse, sofern Java angepasst werden sollte)
- Einrichtung des Clusters und starten der MPJ Express daemons gemäß [User Guide](http://mpj-express.org/guides.html)
- auf die Einrichtung eines passwortlosen SSH Zugangs achten
- Aufruf der Applikation über ```mpjrun.sh -np x -dev hybdev mpj_tspwithaco.jar``` (x entspricht Anzahl der Hosts im Cluster)

Ist eine Ausführung per cluster Konfiguration mit GUI gewünscht, so müssen die Schritte MongoDB etc. zusätzlich ausgeführt werden.

## Live-Demo der Szenarien

Nachfolgend ein paar Bildausschnitte der Live-Demo der einzelnen implementierten Szenarien.

**Das Szenario Ant**

![live_demo_scenario_ant](./live_demo_scenario_ant.PNG?raw=true "live_demo_scenario_ant")

**Das Szenario Multi-TSP**

![live_demo_scenario_multi_tsp](./live_demo_scenario_multi_tsp.PNG?raw=true "live_demo_scenario_multi_tsp")
