'use strict';

const fs = require('fs');
const uuid = require('uuidv4');
var id_list = [];
var id=0;

const postAuftrag = function (database) {
  if (!database) {
    throw new Error('Database is missing.');
  }

  return function (req, res)  {

    id=uuid();
    console.log("Job " + id + " wird angelegt." )
    id_list.push(id);

    var body = req.body;
    var i=0;                                //File-write Status
    var oput=body.MULTI_TSP_SYN_OPT;
    var mode=body.MODE;
    var amount=body.MULTI_TSP_RANDOM_AMOUNT_OPT;

    var job = JSON.parse(body.JOB);         //Koordinaten werden gesondert gespeichert
    // Überprüfung, ob beim Ant Mode ein TSP Job genutzt werden soll
    if(mode.toString().trim() === "ant" && body.GRAPH_MODE.toString().trim() === "tspjob") {
      var settings = {};

      settings["ANT_TSPJOB"] = id+'.json';
    } else
    var settings= JSON.parse('{"ANT_TSPJOB": "default.json"}');
    settings["MODE"]= mode;
    settings["GRAPH_MODE"]=body.GRAPH_MODE;
    settings["MULTI_TSP_SYN_OPT"]= oput;
    settings["MULTI_TSP_RANDOM_AMOUNT_OPT"]=amount;
    delete body ["MULTI_TSP_RANDOM_AMOUNT_OPT"];
    delete body["MULTI_TSP_SYN_OPT"];
    delete body["GRAPH_MODE"];
    delete body["JOB"];
    delete body["MODE"];

    job["id"] = id;
    job["Index"] = id;

    database.createRoute(job, id, err => {
        if (err) {
          console.log("Route " + id + " Database: failed!")
          return res.status(500).send(id + " konnte nicht in die Datenbank eingetragen werden!");
        }
        console.log("Route " + id + " Database: check!")
    });

    database.createSetting(body, id, err => {
        if (err) {
          console.log("Settings Job " + id + " Database: failed!");
          res.writeHead(500);
          res.write(id + " konnte nicht in die Datenbank eingetragen werden!");
        }
        console.log("Settings Job " + id + " Database: OK!");
    });
    body["id"] = id;

    for (var key in body) {   //entfernt "" der JSON-Values
      if (body.hasOwnProperty(key) && isNaN(Number(body[key]))==false) {
        body[key] = Number(body[key]);
      }
    }
    settings["id"]=id;

    var callback= function (status_code) {
      if (status_code==500) {
        res.writeHead(500);
        res.write(id);
        res.end();
      }

      if (status_code==201 && res.statusCode!==500) {
        res.writeHead(201);
        res.write(id);
        res.end();
      }
    };

    if (settings.GRAPH_MODE != "random") {
        job=JSON.stringify(job);
        writeFile(__dirname + "/../../tspjobs/" + id + ".json", job, callback);         //speichert Route
    } else {
        for (i=amount; i>0 ; i--) {
            id=uuid();
            job["Index"] = id;
            var jobJSON=JSON.stringify(job);
            console.log(id);
            writeFile(__dirname + "/../../tspjobs/" + id + ".json", jobJSON, callback);
        }
    }
    writeFile(__dirname + "/../../cfg/tspwithacosettings.json", JSON.stringify(body), callback);
    writeFile(__dirname + "/../../cfg/settings.json", JSON.stringify(settings), callback);         //speichert Route

  };

  function writeFile(path, txt, callback) {

    var callback2= function (value) {
      if (value == 500)
        return callback(value);
      if (value==201) {
        return callback(value);
      }
    };
    fs.writeFile(path, txt, function(err) {
      if (err) {
        console.log("hab error" + path);
        return callback2(500);
      }
      if (path==__dirname + "/../../cfg/settings.json")
        callback2(201);
    });
  };
  function readSingleFile(evt) {
      var f = evt.target.files[0];

      if (f) {
        var r = new FileReader();
        r.onload = function(e) {
  	      contents = e.target.result;
        }
        r.readAsText(f);
      } else {
        alert("Datei konnte nicht geladen werden!");
      }
  }
}

module.exports = postAuftrag;
