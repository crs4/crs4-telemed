.. MOST documentation master file, created by
   sphinx-quickstart on Thu Jul 10 17:16:13 2014.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

================================
Welcome to MOST's documentation!
================================

The MOST project aims to achieve an open, modular and scalable solution for the creation, execution and management of remote clinical consultations with direct interaction between specialists.

The project consists of a set of frameworks that deal with different aspects and technologies useful for the creation of telemedicine applications.

In addition to the frameworks, the project contains a number of helper modules, that should ease developers to build telemedicine applications focusing only on high value-added functionality.

Frameworks
==========

- `Voip <http://most-voip.readthedocs.org/en/latest/>`_: a fast and lightweight library created for handling VOIP sessions;
- `Demographics <http://most-demographics.readthedocs.org/en/latest/>`_: a Django application for patients management.
- `Streaming <http://most-streaming.readthedocs.org/en/latest/>`_: a library to receive and play audio and video streams produced by medical and monitoring devices.

Helpers
=======

- :doc:`users/most_user`:  a Django application for creation and management of administrative users;
- :doc:`users/clinician_user`:  a Django application for creation and management of clinician users;
- :doc:`users/task_group`:  a Django application for creation and management of technical and clinician task groups;
- :doc:`authentication/authentication`:  a consumer/producer library for user authentication based on oauth2 protocol.



Project TOC
===========

.. toctree::
   :maxdepth: 2

   modules
   helpers
   license
   authors


Indices and tables
==================

* :ref:`genindex`
* :ref:`modindex`
* :ref:`search`
