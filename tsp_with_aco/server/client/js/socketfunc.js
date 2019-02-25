$(function () {
    $('#startaco').click(function() {
        if($("#ANZAHL_STAEDTE").val() > 70) {
            alert("Aktuell im GUI nur bis zu 70 Städte möglich!")
            return false;
        }
        if($('#radAntMode').is(':checked')) {
            $("#tspOptimzeResults ul").each(function() {
                $(this).find('li').each(function() {
                    $(this).text("");
                });
            });
            $("#tourlength").text("");
        }
        // ACO starten
        socket.emit("startACO", "Start TSPwithACO...");
        alert("TSPwithACO startet!");
        $("html, body").animate({ scrollTop: $(document).height() }, 3000);
        return false;
    });
    $('#btnTSPJobUpload').on('click', function() {
        myFunction(socket);
    });
    $('#btnDeleteJob').on('click', function() {
        deleteJob();
    });
    $('#btnShowJob').on('click', function() {
        showJob();
    });

    // Multi-TSP Tabs Graph bei Tabauswahl aktualisieren
    $(document).on('click', '.multitsp_tab', function() {
        arMultiTSPTabs.forEach(function(element, index, theArray) {
            if(element.tspjob_index.localeCompare($(this).data('tspjob-index'))) {
                var network = new vis.Network(element.graph_container, element.graph_data, element.graph_options);
                network.moveTo({
                    position: {x: 0, y: 0},
                    offset: {x: -1200/2, y: -600/2},
                    scale: 1
                });
            }
        });
    });

    // Array für MultiTSP Tabs
    var arMultiTSPTabs = [];

    // Socket zum Kommunikation mit der Serverseite
    var socket = io.connect();
    socket.on("myMessage", function(message) {
        console.log(message);
    });

    // Nachricht, dass keine Optimierung gefunden wurde
    socket.on("sendOptimizeResultNotFound", function(message) {
        if(message.aco_no_optimize_result.mode.toString().trim() === "ant")
            $('#mynetwork').append('<p class="important_text">Ameisen konnten keine Optimierung finden!</p>');
        if(message.aco_no_optimize_result.mode.toString().trim() === "multitsp")
            $('#'+message.aco_no_optimize_result.tspjob_index+'_multitsp').append('<p class="important_text">Ameisen konnten keine Optimierung finden!</p>');
    });

    // Aufbau von Tabs für Multi-TSP
    socket.on("sendTSPJobFileNames", function(message) {
        //console.log(message);
        var tabCounter = 1;
        // HTML Tabs bereinigen
        $('#multitsp_tabnav').empty();
        $('#multitsp_content_wrapper').empty();
        // HTML Tabs anhand bereitstehender TSP Jobs (JSON Dateien) anlegen
        message.tspjob_filenames.forEach(function(element, index, theArray) {
            if($('#radGraphRandMode').is(':checked'))
                $('#multitsp_tabnav').append('<li><a href="#tabs-'+tabCounter+'" data-tspjob-index="'+theArray[index].tsp_fielname+'" class="multitsp_tab">Random-Tour ' + index + '</a></li>');
            else
                $('#multitsp_tabnav').append('<li><a href="#tabs-'+tabCounter+'" data-tspjob-index="'+theArray[index].tsp_fielname+'" class="multitsp_tab">Tour ' + index + '</a></li>');
            $('#multitsp_content_wrapper').append('\
            <div id="tabs-'+tabCounter+'">\
                <div id="'+theArray[index].tsp_fielname+'_multitsp" class="mynetworkMultiTSP"></div>\
                <p id="'+theArray[index].tsp_fielname+'_tourlength"></p>\
                <div id="'+theArray[index].tsp_fielname+'_tspOptimzeResults">\
                    <ul>\
                        <li id="'+theArray[index].tsp_fielname+'_average_optimize"></li>\
                        <li id="'+theArray[index].tsp_fielname+'_available_cpu_cores"></li>\
                        <li id="'+theArray[index].tsp_fielname+'_used_cpu_cores"></li>\
                        <li id="'+theArray[index].tsp_fielname+'_marking_weakening"></li>\
                        <li id="'+theArray[index].tsp_fielname+'_probability_random_choice"></li>\
                        <li id="'+theArray[index].tsp_fielname+'_increment"></li>\
                        <li id="'+theArray[index].tsp_fielname+'_tourlength_start"></li>\
                        <li id="'+theArray[index].tsp_fielname+'_tourlength_end"></li>\
                        <li id="'+theArray[index].tsp_fielname+'_length_difference"></li>\
                        <li id="'+theArray[index].tsp_fielname+'_iterations"></li>\
                        <li id="'+theArray[index].tsp_fielname+'_amount_city"></li>\
                        <li id="'+theArray[index].tsp_fielname+'_amount_ants"></li>\
                        <li id="'+theArray[index].tsp_fielname+'_marking_gain"></li>\
                        <li id="'+theArray[index].tsp_fielname+'_scattersize"></li>\
                    </ul>\
                </div>\
            </div>');
            tabCounter++;
        });
        $( "#tabs" ).tabs().addClass( "ui-tabs-vertical ui-helper-clearfix" );
        $( "#tabs li" ).removeClass( "ui-corner-top" ).addClass( "ui-corner-left" );
        $( "#tabs" ).tabs("refresh");
    });

    // Ergebnisse fertiger TSP Optimierungen entgegennehmen und im GUI anzeigen
    socket.on("sendOptimizeResult", function(message) {
        //console.log(message);
        if(message.aco_final.mode.toString().trim() === "ant") {
            $("#average_optimize").text("durchschnittliche Optimierung: " + message.aco_final.average_optimize);
            $("#available_cpu_cores").text("verfügbare CPU-Kerne: " + message.aco_final.available_cpu_cores);
            $("#used_cpu_cores").text("genutzte CPU-Kerne: " + message.aco_final.used_cpu_cores);
            $("#marking_weakening").text("Markierung Abschwächung: " + message.aco_final.marking_weakening);
            $("#probability_random_choice").text("Wahrscheinlichkeit zufällige Auswahl: " + message.aco_final.probability_random_choice);
            $("#increment").text("Schrittweite: " + message.aco_final.increment);
            $("#tourlength_start").text("Tourlänge start: " + message.aco_final.tourlength_start);
            $("#tourlength_end").text("Tourlänge optimiert: " + message.aco_final.tourlength_end);
            $("#length_difference").text("Optimierung der Tour: " + message.aco_final.length_difference);
            $("#iterations").text("ACO-Iterationen: " + message.aco_final.iterations);
            $("#amount_city").text("Anzahl der Städte: " + message.aco_final.amount_city);
            $("#amount_ants").text("Anzahl der Ameisen: " + message.aco_final.amount_ants);
            $("#marking_gain").text("Faktor Markierung Verstärkung: " + message.aco_final.marking_gain);
            $("#scattersize").text("Größe der verteilten Anteile pro Prozess: " + message.aco_final.scattersize);
            alert("TSPwithACO finished!");
        }

        if(message.aco_final.mode.toString().trim() === "multitsp") {
            // fertige Tabs grün färben
            $('a[data-tspjob-index="'+message.aco_final.tspjob_index+'"]').parent().css('background', 'green');
            $("#"+message.aco_final.tspjob_index+"_average_optimize").text("durchschnittliche Optimierung: " + message.aco_final.average_optimize);
            $("#"+message.aco_final.tspjob_index+"_available_cpu_cores").text("verfügbare CPU-Kerne: " + message.aco_final.available_cpu_cores);
            $("#"+message.aco_final.tspjob_index+"_used_cpu_cores").text("genutzte CPU-Kerne: " + message.aco_final.used_cpu_cores);
            $("#"+message.aco_final.tspjob_index+"_marking_weakening").text("Markierung Abschwächung: " + message.aco_final.marking_weakening);
            $("#"+message.aco_final.tspjob_index+"_probability_random_choice").text("Wahrscheinlichkeit zufällige Auswahl: " + message.aco_final.probability_random_choice);
            $("#"+message.aco_final.tspjob_index+"_increment").text("Schrittweite: " + message.aco_final.increment);
            $("#"+message.aco_final.tspjob_index+"_tourlength_start").text("Tourlänge start: " + message.aco_final.tourlength_start);
            $("#"+message.aco_final.tspjob_index+"_tourlength_end").text("Tourlänge optimiert: " + message.aco_final.tourlength_end);
            $("#"+message.aco_final.tspjob_index+"_length_difference").text("Optimierung der Tour: " + message.aco_final.length_difference);
            $("#"+message.aco_final.tspjob_index+"_iterations").text("ACO-Iterationen: " + message.aco_final.iterations);
            $("#"+message.aco_final.tspjob_index+"_amount_city").text("Anzahl der Städte: " + message.aco_final.amount_city);
            $("#"+message.aco_final.tspjob_index+"_amount_ants").text("Anzahl der Aneisen: " + message.aco_final.amount_ants);
            $("#"+message.aco_final.tspjob_index+"_marking_gain").text("Faktor Markierung Verstärkung: " + message.aco_final.marking_gain);
            $("#"+message.aco_final.tspjob_index+"_scattersize").text("Größe der verteilten Anteile pro Prozess: " + message.aco_final.scattersize);
        }
    });

    // Ergebnisse einer ACO Iteration entgegennehmen und im GUI anzeigen
    socket.on("sendNodes", function(message) {
        //console.log(message);
        if(message.aco_iteration.params.mode.toString().trim() === "ant")
            $("#tourlength").text("Tourlänge: " + message.aco_iteration.params.tourlength);
        if(message.aco_iteration.params.mode.toString().trim() === "multitsp")
            $("#"+message.aco_iteration.params.tspjob_index+"_tourlength").text("Tourlänge: " + message.aco_iteration.params.tourlength);
        // Zähler zur Darstellung des vollständigen Graphen
        var nodeCounter = 0;
        // Anlegen des Graphens einer ACO Iteration
        var options = {
            layout: {
                hierarchical: {
                    sortMethod: 'directed'
                }
            }
        };
        var dataNodes = new vis.DataSet(options);
        var jsonObjectNodes = message.aco_iteration.nodes;

        for(var element in jsonObjectNodes) {
            //dataNodes.add({id: jsonObjectNodes[element].id, label: jsonObjectNodes[element].label});
            dataNodes.add(jsonObjectNodes[element]);
            nodeCounter++;
        }

        // Array für Knoten anlegen
        var nodes = dataNodes;

        var dataEdges = new vis.DataSet(options);
        var jsonObjectEdges = message.aco_iteration.edges;

        for(var element in jsonObjectEdges) {
            //dataEdges.add({id: jsonObjectEdges[element].id, label: jsonObjectEdges[element].label});
            for(var i = 0;i<nodeCounter;i++) {
                if(i!=jsonObjectEdges[element].from && i!=jsonObjectEdges[element].to) {
                    dataEdges.add({from:jsonObjectEdges[element].from,to:i,color:'#D8D8D8'});
                }
            }
        }

        for(var element in jsonObjectEdges) {
            dataEdges.add(jsonObjectEdges[element]);
        }

        // Array für Kanten anlegen
        var edges = dataEdges;

        // Netzwerk für Graph anlegen
        if(message.aco_iteration.params.mode.toString().trim() === "ant")
            var container = document.getElementById('mynetwork');
        if(message.aco_iteration.params.mode.toString().trim() === "multitsp")
            var container = document.getElementById(message.aco_iteration.params.tspjob_index+'_multitsp');
        var data = {
            nodes: nodes,
            edges: edges
        };
        var options = {
            edges: {
                smooth: false
            },
            interaction: {
                dragNodes: false,// do not allow dragging nodes
                zoomView: false, // do not allow zooming
                dragView: false  // do not allow dragging
            }
        };

        if(message.aco_iteration.params.mode.toString().trim() === "ant") {
            var network = new vis.Network(container, data, options);
            network.moveTo({
                position: {x: 0, y: 0},
                offset: {x: -1200/2, y: -600/2},
                scale: 1
            });
        }

        if(message.aco_iteration.params.mode.toString().trim() === "multitsp") {
            // Tab Objekt anlegen
            var multiTSPTab = {
                tspjob_index : message.aco_iteration.params.tspjob_index,
                graph_data : data,
                graph_container : container,
                graph_options : options
            }
            var newTSPTab = true;

            // Inhalte für Tab aktualisieren
            arMultiTSPTabs.forEach(function(element, index, theArray){
                if(element.tspjob_index === message.aco_iteration.params.tspjob_index) {
                    theArray[index].graph_data = data;
                    newTSPTab = false;
                }
            });

            // neues Tab Objekt hinzufügen, sofern noch nicht bekannt
            if(newTSPTab==true)
                arMultiTSPTabs.push(multiTSPTab);

            // jedes Tab mit vis.js Graph Objekt live aktualisieren
            arMultiTSPTabs.forEach(function(element, index, theArray){
                if(element.tspjob_index.localeCompare($(this).data('tspjob-index'))) {
                    if(message.aco_iteration.params.graph_mode.toString().trim() === "random")
                        $('a[data-tspjob-index="'+element.tspjob_index+'"]').text("Random-Tour " + index);
                    else
                        if(element.tspjob_index === message.aco_iteration.params.tspjob_index)
                            $('a[data-tspjob-index="'+element.tspjob_index+'"]').text(message.aco_iteration.params.tourname);
                    var network = new vis.Network(element.graph_container, element.graph_data, element.graph_options);
                    network.moveTo({
                        position: {x: 0, y: 0},
                        offset: {x: -1200/2, y: -600/2},
                        scale: 1
                    });
                }
            });
        }
    });
});
