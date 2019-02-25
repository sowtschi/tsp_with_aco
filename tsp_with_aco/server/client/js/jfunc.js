document.getElementById('fileinput').addEventListener('change', readSingleFile, true);
window.onbeforeunload = function(e) {

	var selectobject=document.getElementById("Jobs");
		for (var i=0; i<selectobject.length; i++){
			job = selectobject.options[i].value;
			var path = "./job/" + job;
				$.ajax({
					url: 'http://localhost:3000/job/'+ job,
					type: 'DELETE',
					success: function(result) {
						// TSP Job Filenames beim Server anfragen
						alert(result + " erfolgreich geloescht!")
						$("#Jobs option:selected").remove();
						socket.emit("getJobFileNames");
					},
					 error: function (xhr, status, error) {
						 alert("Reste von: " + $("#Jobs option:selected").text()) + " entfernt!";
						 $("#Jobs option:selected").remove();
					 }
				});
	}


};

function myFunction(socket) {

	var params_list = [];
	var mode = "multitsp";
	var MULTI_TSP_SYN_OPT="syn";
	var GRAPH_MODE="random";
	var x = document.getElementById("Jobs");
	params_list.push(document.getElementById("ANZAHL_STAEDTE").value);
	params_list.push(document.getElementById("ANZAHL_AMEISEN").value);
	params_list.push(document.getElementById("WAHRSCHEINLICHKEIT_ZUFAELLIGE_AUSWAHL").value);
	params_list.push(document.getElementById("MARKIERUNG_ABSCHWAECHUNG").value);
	params_list.push(document.getElementById("MARKIERUNG_VERSTAERKUNG").value);
	params_list.push(document.getElementById("SCHRITTWEITE").value);
	params_list.push(document.getElementById("MINIMALE_ENTFERNUNG_DER_STAEDTE").value);
	params_list.push(document.getElementById("ACO_ITERATIONS").value);
	params_list.push(document.getElementById("ACO_REFRESH_RATE").value);
	params_list.push(document.getElementById("ACO_REFRESH_DELAY").value);
	params_list.push(document.getElementById("MULTI_TSP_RANDOM_AMOUNT_OPT").value);

	if (document.getElementById('radGraphTSPJobMode').checked) {
			GRAPH_MODE="tspjob";
	}

	if (document.getElementById('radAntMode').checked) {
			mode="ant";
	}

	if (document.getElementById('radSynOptAsync').checked) {
		MULTI_TSP_SYN_OPT="asyn";
	}

	var http = new XMLHttpRequest();
	var url = "./auftrag";
	if (GRAPH_MODE != "random") {
		try {
			contents=JSON.parse(contents);
		} catch(e) {
				alert("Eintragen fehlgeschlagen \n  Der geladene TSP-Job kann nicht verarbeitet werden! "); // error in the above string (in this case, yes)!
				return;
		}
	} else {
		contents=JSON.parse('{}');
	}
	i=0
	for (i in params_list) {
		if (params_list[i].indexOf(",") >= 0) {
			params_list[i] = params_list[i].replace(",",".");		//Kommas in Punkte umwandeln
		}
		var failed = isNumeric(params_list[i]);
		if (failed==99) {
			return;
		}
	}

	var params = 'ANZAHL_STAEDTE=' +params_list[0] +'&ANZAHL_STAEDTE_MAXIMAL='+ 80 + '&ANZAHL_AMEISEN='+params_list[1]
	+ '&WAHRSCHEINLICHKEIT_ZUFAELLIGE_AUSWAHL=' + params_list[2] + '&MARKIERUNG_ABSCHWAECHUNG=' + params_list[3]
	+ '&MARKIERUNG_VERSTAERKUNG='+params_list[4] + '&SCHRITTWEITE=' +params_list[5]
	+ '&MINIMALE_ENTFERNUNG_DER_STAEDTE=' + params_list[6] + '&ACO_ITERATIONS=' + params_list[7]
	+ '&ACO_REFRESH_RATE=' + params_list[8] + '&ACO_REFRESH_DELAY=' + params_list[9]
	+ '&MODE='+ mode + '&MULTI_TSP_SYN_OPT='+ MULTI_TSP_SYN_OPT + '&JOB=' + JSON.stringify(contents) + '&MULTI_TSP_RANDOM_AMOUNT_OPT=' + params_list[10] + '&GRAPH_MODE=' + GRAPH_MODE;





	if (params_list[2] > 0 && params_list[2] < 1 ) {	//	WAHRSCHEINLICHKEIT_ZUFAELLIGE_AUSWAHL zwischen 0 und 1
		$.post('./auftrag',params, function (data) {

		}).done(function(data, statusText, xhr){
			// Tabs nur im Multi-TSP Mode aufbauen
			if (mode != "ant") {
				// TSP Job Filenames beim Server anfragen
				socket.emit("getJobFileNames");
			}
			var option = document.createElement("option");
			option.text = data;
			x.add(option);
			alert("Job: " + data + " erfolgreich angelegt!");

		}).error(function (jqXHR, textStatus, error) {
			alert("Fehler beim Eintragen der Daten! \n");
		});
	} else {
		alert("Wahrscheinlichkeit zufaellige Auswahl muss zwischen 0 und 1 liegen! \n Kalibrierung abgebrochen!" + params_list[3]);
	}
}

function isNumeric(element) {
		if (isNaN(Number(element))==true) {
			alert( element + " ist kein g" + unescape("%FC") + "ltiger Wert! \n Kalibrierung abgebrochen!");
			return 99;
		} else if ("" === element) {
			alert( "Es fehlt mindestens ein Parameter! \nKalibrierung abgebrochen!");
			return 99;
		}

}

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

function deleteJob() {

	job = $("#Jobs :selected").text();

	var result = confirm("Want to delete?");

	if (result) {
		if (!job) {
			alert("Kein Job zum loeschen gefunden");
	} else {
			var path = "./job/" + job;
	}

		$.ajax({
	    url: 'http://localhost:3000/job/'+ job,
	    type: 'DELETE',
	    success: function(result) {
				// TSP Job Filenames beim Server anfragen
				alert(result + " erfolgreich geloescht!")
				$("#Jobs option:selected").remove();
				socket.emit("getJobFileNames");
	    },
			 error: function (xhr, status, error) {
				 alert("Reste von: " + $("#Jobs option:selected").text()) + " entfernt!";
				 $("#Jobs option:selected").remove();
			 }
		});
	}

}

function showJob() {
	job = $("#Jobs :selected").text();
	var path = "./job/" + job;
	if (!job) {
		alert("Kein Job zum Anzeigen gefunden");
		return;
	} else {
	}
	$.ajax({
    url: 'http://localhost:3000/job/'+ job,
    type: 'GET',
    success: function(result) {
			var wnd = window.open("about:blank", "", "width=600,height=400");
			wnd.document.write(result);
    },
		 error: function (xhr, status, error) {
			 alert($("#Jobs option:selected").text()) + " kann nicht angezeigt werden!";
		 }
	});
}
