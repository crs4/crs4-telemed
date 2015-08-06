/*!
 * Project MOST - Moving Outcomes to Standard Telemedicine Practice
 * http://most.crs4.it/
 *
 * Copyright 2014, CRS4 srl. (http://www.crs4.it/)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * See license-GPLv2.txt or license-MIT.txt
 */

$(function() {
    $('#exam_func_1').click(function(){
        console.log('in exam_func_1 click event');
        $( '#api-launcher' ).load('/exam/new/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#exam_func_2').click(function(){
        console.log('in exam_func_2 click event');
        $( '#api-launcher' ).load('/exam/edit/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#exam_func_3').click(function(){
        console.log('in exam_func_3 click event');
        $( '#api-launcher' ).load('/exam/delete/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#exam_func_4').click(function(){
        console.log('in exam_func_4 click event');
        $( '#api-launcher' ).load('/exam/search/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#exam_func_5').click(function(){
        console.log('in exam_func_5 click event');
        $( '#api-launcher' ).load('/exam/get_info/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#exam_func_6').click(function(){
        console.log('in exam_func_6 click event');
        $( '#api-launcher' ).load('/exam/list_sections/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#exam_func_7').click(function(){
        console.log('in exam_func_7 click event');
        $( '#api-launcher' ).load('/exam/add_section/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#exam_func_8').click(function(){
        console.log('in exam_func_8 click event');
        $( '#api-launcher' ).load('/exam/remove_section/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#exam_func_9').click(function(){
        console.log('in exam_func_9 click event');
        $( '#api-launcher' ).load('/exam/set_clinician/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});
