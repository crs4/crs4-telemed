Most Demo
=========

This repository provides a demo application of the MOST Framework. It implements a complete teleconsultation system where an echographist, provided with a mobile phone, can require a teleconsultation with a specialist during the execution of an exam.  The specialist can interact with the echographist by a mobile application, that shows 2 real time A/V streams: the first is the A/V output of  the echocardiograph screen, the second is the video stream incoming from a web camera located in the patient room. The echographist and the specialist can communicate each other, and the specialist can remotely control the position  and the zoom of the web camera, in order to better drive the ecographist during the exam execution. At the end of the teleconsultation, the specialist is required to fill out a medical report. This application internally use the following MOST libraries:

 * most for managing clinicians, taskgroups and clients authentication
 * most-voip for performing audio calls
 * most-streaming for managing audio/video streams
 * most-visualization for providing visual widgets for interacting with A/V streams
 * most-report for building and managing medical reports

Documentation
=============

Documentation of the demo can be found `here.  <http://most-demo.readthedocs.org/>`_


About the MOST Project
======================

The MOST project aims to achieve an open, modular and scalable solution for the creation, execution and management of remote clinical consultations with direct interaction between specialists.  

The project consists of a set of frameworks that deal with different aspects and technologies useful for the creation of telemedicine applications.

Available MOST Frameworks
=========================
  * `MOST <https://github.com/crs4/most>`_  (a set of helper django modules for managing users, clinicians,taskgroups and clients  authentication)
  * `MOST-Voip  <https://github.com/crs4/most-voip>`_  (a fast and lightweight library for handling VOIP sessions)
  * `MOST-Streaming  <https://github.com/crs4/most-streaming>`_  (a library for managing audio/video streams)
  * `MOST-Visualization  <https://github.com/crs4/most-visualization>`_  (a library for providing mobile applications with visual widgets for interacting with A/V streams)
  * `MOST-Report  <https://github.com/crs4/most-report>`_ (a library for managing clinical models)
  * `MOST-Demographics  <https://github.com/crs4/most-demographics>`_ (a Django application for patients management)
  * `MOST-MedicalRecords  <https://github.com/crs4/most-medicalrecords>`_ (a frontend REST api for accessing to demographics and clinical data of patients)
  * `MOST-Demo  <https://github.com/crs4/most-demo>`_ (a DEMO Application showing the main features of the MOST frameworks)


License
=======

::

     Project MOST - Moving Outcomes to Standard Telemedicine Practice
     http://most.crs4.it/
    
     Copyright 2014, CRS4 srl. (http://www.crs4.it/)
     
     Dual licensed under the MIT or GPL Version 2 licenses.
     See license-GPLv2.txt or license-MIT.txt

GPL2: https://www.gnu.org/licenses/gpl-2.0.txt

MIT: http://opensource.org/licenses/MIT


Detailed Dual Licensing Info
============================

The MOST-Voip API is licensed under both General Public License (GPL) version 2 and the MIT licence. In practical sense, this means:

    * if you are developing Open Source Software (OSS) based on MOST-Voip, chances are you will be able to use MOST-Voip under GPL. Note that the Most-Voip Library depends on the PJSIP API, so  please double check `here <http://www.pjsip.org/licensing.htm>`_ for OSS license compatibility with GPL.
    * alternatively, you can release your application under MIT licence, provided that you have followed the guidelines of the PJSIP licence explained `here <http://www.pjsip.org/licensing.htm>`_.


