'use strict';

const http= require('http');
const socketio = require("socket.io");
const getApp = require('./lib/getApp'),
      database = require('./database'),
      exec = require('child_process').exec;
const mongoUrl =process.env.MONGO_URL || 'mongodb://admin:secret@localhost:27017/admin',
      port = process.env.PORT || 3000;
const app = getApp(database);
const server = http.createServer(app);
const io = require('socket.io').listen(server);
const spawn = require('child_process').spawn;
const fs = require('fs');
const tspJobsFolder = './tspjobs/';

// Datenbank initialisieren und bei Fehler abbrechen
database.initialize(mongoUrl, err => {
    if (err) {
      console.log('Failed to connect to database.', {err});
      process.exit(1);
    }
});

// Socket konfigurieren
io.sockets.on("connection", function(socket) {
    console.log("socket connected");

    socket.emit("myMessage", "Hi from Server!");
    socket.on("getJobFileNames", function() {
        // JSON Objekt für Dateinamen
        var jsonTSPFilenames = {};
        const key = 'tspjob_filenames';
        // leeres Array, welches per push() gefüllt wird
        jsonTSPFilenames[key] = [];
        // TSP Jobs ermitteln und Client bekannt geben
        fs.readdir(tspJobsFolder, (err, files) => {
            files.forEach(function(filename) {
                // Dateinamen ohne json Erweiterung sichern
                if(filename.toString().trim() !== 'default.json') {
                    filename = filename.substr(0, filename.lastIndexOf("."));
                    jsonTSPFilenames[key].push({tsp_fielname: filename});
                }
            });
            // Client die Dateinamen bekannt machen
            socket.emit("sendTSPJobFileNames", jsonTSPFilenames);
        });

    });
    // ACO starten und MPI jar Prozess entsprechend ausführen
    socket.on("startACO", function(request) {
        const tspwithACOjarCommand_win = `mpjrun.bat -np ${getAvailableCPUCore()} mpj_tspwithaco.jar`;
        const tspwithACOjarCommand_unix = `mpjrun.sh -np ${getAvailableCPUCore()} mpj_tspwithaco.jar`;
        var javaProcess = null;
        if(process.platform === "win32") {
            // die Option maxBuffer muss erhöht werden (default 200*1024) aufgrund der vielen stdout Ausgaben
            javaProcess = spawn(process.env.comspec, ['/C', tspwithACOjarCommand_win]);
            //javaProcess = exec("cmd.exe /C " + tspwithACOjarCommand_win);
        } else {
            // die Option maxBuffer muss erhöht werden (default 200*1024) aufgrund der vielen stdout Ausgaben
            javaProcess = exec(tspwithACOjarCommand_unix, { maxBuffer: 1000*1024 });
        }
        // stdout Ausgaben des Java Prozess abfangen (i.d.R. JSON Output für das GUI)
        javaProcess.stdout.on('data', function (data) {
            //console.log('stdout: ' + data.toString());
            // Rückgabe ist i.d.R. JSON, daher wird versucht zu parsen
            try {
                var jsonInput = JSON.parse(data.toString());
                // JSON Rückgabe einer "fertigen" TSP Optimierung per Socket an das GUI
                if(jsonInput.hasOwnProperty('aco_final')) {
                    //console.log(jsonInput);
                    // aus performance Gründen zuerst den Client informieren
                    socket.emit("sendOptimizeResult", jsonInput);
                    var id=jsonInput.aco_final.tspjob_index;
                    database.createFinal(jsonInput, err => {
                        if (err) {
                            return console.log("FINAL_ITERATION: " + id.toString() + " konnte nicht eingetragen werden!");
                        }
                        return console.log("FINAL_ITERATION: " + id.toString() + " erfolgreich eingetragen!");
                    });
                }
                // JSON Rückgabe einer ACO Iteration per Socket an das GUI
                if(jsonInput.hasOwnProperty('aco_iteration')) {
                    // aus performance Gründen zuerst den Client informieren
                    socket.emit("sendNodes", jsonInput);
                    var id=jsonInput.aco_iteration.params.tspjob_index;
                    database.createIteration(jsonInput, err => {
                        if (err) {
                            return console.log("ACO_ITERATION: " + id.toString() + " konnte nicht eingetragen werden!");
                        }
                        //return console.log("ACO_ITERATION: " + id.toString() + " erfolgreich eingetragen!");
                    });
                }
                // JSON Rückgabe wenn ACO keine Optimierung ermitteln konnte
                if(jsonInput.hasOwnProperty('aco_no_optimize_result')) {
                    // aus performance Gründen zuerst den Client informieren
                    socket.emit("sendOptimizeResultNotFound", jsonInput);
                }
            } catch (err) {
                //console.log(err.message);
            };
        });

        // stderr Ausgaben des Java Prozess abfangen (Fehler)
        javaProcess.stderr.on('data', function (data) {
            console.log('stderr: ' + data.toString());
        });

        // exit Ereignis des Java Prozess abfangen
        javaProcess.on('exit', function (code) {
            try {
                console.log('child process exited with code ' + code.toString());
                // TSP Jobs löschen
                fs.readdir(tspJobsFolder, (err, files) => {
                    files.forEach(function(filename) {
                        if(filename.toString().trim() !== 'default.json') {
                            fs.unlink(tspJobsFolder+filename, function(error) {
                                if (error)
                                    console.log(error);
                                else
                                    console.log('Deleted TSP Job File: ' + filename);
                            });
                        }
                    });
                });
            } catch (err) {
                console.log('code was not normal, it was ' + code + ' or error: ' + err);
            }
        });

        // close Ereignis des Java Prozess abfangen (gibt neben Code auch noch das Signal zurück)
        javaProcess.on('close', function (code, signal) {
            try {
                console.log(`child process exited with code: ${code} and signal: ${signal}`);
            } catch (err) {
                console.log(code + " " + signal);
            }
        });

        // error Ereignis des Java Prozess abfangen
        javaProcess.on('error', function(err) {
            console.log('Failed to start child process.');
            console.log(err);
        });
    });
});

const requestHandler = (request, response) => {
    console.log(request.url)
    response.end('Hello Node.js Server!')
}

// Webserver starten
server.listen(port,() => {
    console.log('Server is started.', {port});
});

// Funktion zur Rückgabe aller verfügaben CPU Kerne
function getAvailableCPUCore() {
    const tspwithACOjarCommand_win = 'mpjrun.bat -np 1 mpj_tspwithaco.jar';
    const tspwithACOjarCommand_unix = `mpjrun.sh -np 1 mpj_tspwithaco.jar`;
    var javaProcess = null;
    if(process.platform === "win32")
        javaProcess = spawn(process.env.comspec, ['/C', tspwithACOjarCommand_win]);
    else
        javaProcess = exec(tspwithACOjarCommand_unix);
    // stdout Ausgaben des Java Prozess abfangen (i.d.R. JSON Output für das GUI)
    javaProcess.stdout.on('data', function (data) {
        //console.log('stdout: ' + data.toString());
        // Rückgabe ist i.d.R. JSON, daher wird versucht zu parsen
        try {
            var jsonInput = JSON.parse(data.toString());
            // JSON Rückgabe einer "fertigen" TSP Optimierung per Socket an das GUI
            if(jsonInput.hasOwnProperty('available_processors')) {
                return jsonInput.available_processors;
            }
        } catch (err) {
            //console.log(err.message);
        };
    });
}
