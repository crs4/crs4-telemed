/*!
 * Project MOST - Moving Outcomes to Standard Telemedicine Practice
 * http://most.crs4.it/
 *
 * Copyright 2014, CRS4 srl. (http://www.crs4.it/)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * See license-GPLv2.txt or license-MIT.txt
 */

$(function() {
    $('#visit_func_1').click(function(){
        console.log('in visit_func_1 click event');
        $( '#api-launcher' ).load('/visit/new/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#visit_func_2').click(function(){
        console.log('in visit_func_2 click event');
        $( '#api-launcher' ).load('/visit/edit/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#visit_func_3').click(function(){
        console.log('in visit_func_3 click event');
        $( '#api-launcher' ).load('/visit/delete/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#visit_func_4').click(function(){
        console.log('in visit_func_4 click event');
        $( '#api-launcher' ).load('/visit/search/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#visit_func_5').click(function(){
        console.log('in visit_func_5 click event');
        $( '#api-launcher' ).load('/visit/get_info/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#visit_func_6').click(function(){
        console.log('in visit_func_6 click event');
        $( '#api-launcher' ).load('/visit/get_full_info/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#visit_func_7').click(function(){
        console.log('in visit_func_7 click event');
        $( '#api-launcher' ).load('/visit/list_exams/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#visit_func_8').click(function(){
        console.log('in visit_func_8 click event');
        $( '#api-launcher' ).load('/visit/add_exam/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#visit_func_9').click(function(){
        console.log('in visit_func_9 click event');
        $( '#api-launcher' ).load('/visit/remove_exam/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#visit_func_10').click(function(){
        console.log('in visit_func_10 click event');
        $( '#api-launcher' ).load('/visit/set_parent_visit/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});
