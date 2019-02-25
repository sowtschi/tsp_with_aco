$(function () {
    // standard GUI Ausgabe
    $('#output_antmode_wrapper').hide();
    $('#output_multitspmode_wrapper').show();
    $('#wrapper_MULTI_TSP_RANDOM_AMOUNT_OPT').hide();
    $('#wrapper_MINIMALE_ENTFERNUNG_DER_STAEDTE').hide();
    $('#wrapper_ANZAHL_STAEDTE').hide();

    // GUI Interaktionen
    $("#radAntMode").click(function() {
        $('tr:not(.mode_ant)').hide();
        $('tr.mode_ant').show();
        $('#output_multitspmode_wrapper').hide();
        $('#output_antmode_wrapper').show();
        if($('#radGraphTSPJobMode').is(':checked')) {
            $('#wrapper_ANZAHL_STAEDTE').hide();
            $('#wrapper_fileinput').show();
            $('#wrapper_MINIMALE_ENTFERNUNG_DER_STAEDTE').hide();
            $('#btnTSPJobUpload').html("TSP Job uploaden und Einstellungen speichern");
        }
        if($('#radGraphRandMode').is(':checked')) {
            $('#wrapper_ANZAHL_STAEDTE').show();
            $('#wrapper_fileinput').hide();
            $('#wrapper_MINIMALE_ENTFERNUNG_DER_STAEDTE').show();
            $('#btnTSPJobUpload').html("Einstellungen speichern");
        }
    });

    $("#radMultiTSPMode").click(function() {
        $('tr:not(.mode_multitsp)').hide();
        $('tr.mode_multitsp').show();
        $('#output_antmode_wrapper').hide();
        $('#output_multitspmode_wrapper').show();
        if($('#radGraphTSPJobMode').is(':checked')) {
            $('tr:not(.graph_mode_tspjob)').hide();
            $('#wrapper_fileinput').show();
            $('#wrapper_MINIMALE_ENTFERNUNG_DER_STAEDTE').hide();
            $('#wrapper_ANZAHL_STAEDTE').hide();
            $('#btnTSPJobUpload').html("TSP Job uploaden und Einstellungen speichern");
        }
        if($('#radGraphRandMode').is(':checked')) {
            $('tr:not(.graph_mode_random)').hide();
            $('#wrapper_fileinput').hide();
            $('#wrapper_MINIMALE_ENTFERNUNG_DER_STAEDTE').show();
            $('#wrapper_ANZAHL_STAEDTE').show();
            $('#btnTSPJobUpload').html("Einstellungen speichern");
        }
    });

    $('#radGraphRandMode').click(function() {
        $('tr:not(.graph_mode_random)').hide();
        $('tr.graph_mode_random').show();
        $('#btnTSPJobUpload').html("Einstellungen speichern");
        if($('#radAntMode').is(':checked')) {
            $('#wrapper_MULTI_TSP_SYN_OPT').hide();
            $('#wrapper_MULTI_TSP_RANDOM_AMOUNT_OPT').hide();
        }
    });

    $('#radGraphTSPJobMode').click(function() {
        $('tr:not(.graph_mode_tspjob)').hide();
        $('tr.graph_mode_tspjob').show();
        $('#wrapper_delete_job').show();
        $('#wrapper_ANZAHL_STAEDTE').hide();
        $('#btnTSPJobUpload').html("TSP Job uploaden und Einstellungen speichern");
        if($('#radAntMode').is(':checked')) {
            $('#wrapper_MULTI_TSP_SYN_OPT').hide();
            $('#wrapper_delete_job').hide();
        }
    });
});