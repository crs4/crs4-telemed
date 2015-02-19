/*!
 * Project MOST - Moving Outcomes to Standard Telemedicine Practice
 * http://most.crs4.it/
 *
 * Copyright 2014, CRS4 srl. (http://www.crs4.it/)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * See license-GPLv2.txt or license-MIT.txt
 */

$(function() {
    $('#task_group_func_1').click(function(){
        console.log('in task_group_func_1 click event');
        //$( '#api-launcher-body' ).html($( this ).html());
        $( '#api-launcher' ).load('/task_group/is_provider/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#task_group_func_2').click(function(){
        console.log('in task_group_func_2 click event');
        //$( '#api-launcher-body' ).html($( this ).html());
        $( '#api-launcher' ).load('/task_group/set_provider/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#task_group_func_3').click(function(){
        console.log('in task_group_func_3 click event');
        //$( '#api-launcher-body' ).html($( this ).html());
        $.ajax({
            url: '/users/task_group/list_available_states/',
            type: 'GET',
            dataType: 'text',
            success: function (data, textStatus, jqXHR) {
                var jsonData = JSON.parse(data);
                var beautifiedData = JSON.stringify(jsonData, null, 4);
                $('#task_group_results_body').html('<div><pre class="pre-scrollable">' + beautifiedData + '</pre></div>');
                if (jsonData.success) {
                    if ($('#task_group_results').hasClass('panel-danger')) {
                        $('#task_group_results').removeClass('panel-danger');
                    }
                    if (!$('#task_group_results').hasClass('panel-success')) {
                        $('#task_group_results').addClass('panel-success');
                    }
                }
                else {
                    $('#task_group_results_body').html('<div>' + data.errors + '</div>');
                    if ($('#task_group_results').hasClass('panel-success')) {
                        $('#task_group_results').removeClass('panel-success');
                    }
                    if (!$('#task_group_results').hasClass('panel-danger')) {
                        $('#task_group_results').addClass('panel-danger');
                    }
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                $('#task_group_result_body').html('<div>' + textStatus + ': ' + errorThrown + '</div>');
                if ($('#task_group_results').hasClass('panel-success')) {
                    $('#task_group_results').removeClass('panel-success');
                }
                if (!$('#task_group_results').hasClass('panel-danger')) {
                    $('#task_group_results').addClass('panel-danger');
                }
            }
        });
    });
});

$(function() {
    $('#task_group_func_4').click(function(){
        console.log('in task_group_func_4 click event');
        //$( '#api-launcher-body' ).html($( this ).html());
        $( '#api-launcher' ).load('/task_group/set_active_state/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#task_group_func_5').click(function(){
        console.log('in task_group_func_5 click event');
        //$( '#api-launcher-body' ).html($( this ).html());
        $( '#api-launcher' ).load('/task_group/add_user/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#task_group_func_6').click(function(){
        console.log('in task_group_func_6 click event');
        //$( '#api-launcher-body' ).html($( this ).html());
        $( '#api-launcher' ).load('/task_group/remove_user/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#task_group_func_7').click(function(){
        console.log('in task_group_func_7 click event');
        //$( '#api-launcher-body' ).html($( this ).html());
        $( '#api-launcher' ).load('/task_group/list_users/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#task_group_func_8').click(function(){
        console.log('in task_group_func_8 click event');
        //$( '#api-launcher-body' ).html($( this ).html());
        $( '#api-launcher' ).load('/task_group/add_related_task_group/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#task_group_func_9').click(function(){
        console.log('in task_group_func_9 click event');
        //$( '#api-launcher-body' ).html($( this ).html());
        $( '#api-launcher' ).load('/task_group/remove_related_task_group/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#task_group_func_10').click(function(){
        console.log('in task_group_func_10 click event');
        //$( '#api-launcher-body' ).html($( this ).html());
        $( '#api-launcher' ).load('/task_group/list_related_task_group/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#task_group_func_11').click(function(){
        console.log('in task_group_func_11 click event');
        //$( '#api-launcher-body' ).html($( this ).html());
        $( '#api-launcher' ).load('/task_group/has_clinicians/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#task_group_func_12').click(function(){
        console.log('in task_group_func_12 click event');
        //$( '#api-launcher-body' ).html($( this ).html());
        $( '#api-launcher' ).load('/task_group/list_clinicians/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#task_group_func_13').click(function(){
        console.log('in task_group_func_13 click event');
        //$( '#api-launcher-body' ).html($( this ).html());
        $( '#api-launcher' ).load('/task_group/has_clinician_provider/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#task_group_func_14').click(function(){
        console.log('in task_group_func_14 click event');
        //$( '#api-launcher-body' ).html($( this ).html());
        $( '#api-launcher' ).load('/task_group/list_clinician_providers/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#task_group_func_15').click(function(){
        console.log('in task_group_func_15 click event');
        //$( '#api-launcher-body' ).html($( this ).html());
        $( '#api-launcher' ).load('/task_group/search/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#task_group_func_16').click(function(){
        console.log('in task_group_func_16 click event');
        //$( '#api-launcher-body' ).html($( this ).html());
        $( '#api-launcher' ).load('/task_group/new/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});