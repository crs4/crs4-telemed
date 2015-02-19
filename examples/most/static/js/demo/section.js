/*!
 * Project MOST - Moving Outcomes to Standard Telemedicine Practice
 * http://most.crs4.it/
 *
 * Copyright 2014, CRS4 srl. (http://www.crs4.it/)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * See license-GPLv2.txt or license-MIT.txt
 */

$(function() {
    $('#section_func_1').click(function(){
        console.log('in section_func_1 click event');
        $( '#api-launcher' ).load('/section/new/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#section_func_2').click(function(){
        console.log('in section_func_2 click event');
        $( '#api-launcher' ).load('/section/edit/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#section_func_3').click(function(){
        console.log('in section_func_3 click event');
        $( '#api-launcher' ).load('/section/delete/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#section_func_4').click(function(){
        console.log('in section_func_4 click event');
        $( '#api-launcher' ).load('/section/search/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});

$(function() {
    $('#section_func_5').click(function(){
        console.log('in section_func_5 click event');
        $( '#api-launcher' ).load('/section/get_info/', {'caller':$( this ).text()}, function() {
            console.log('loaded');
            $( '#api-launcher' ).modal().show();
        })
    });
});
