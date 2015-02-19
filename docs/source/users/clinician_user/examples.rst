Examples
~~~~~~~~

**Run the following login API before run others:**

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/user/login/

    from helper import *


    USER_DATA = {
        'username': 'admintest',
        'password': 'admintest',
    }

    user = compose_post_request('/users/user/login/', USER_DATA)
    print_response_data('user', user)

ClinicianUser module provides the following **REST API** (run login api before run the following):

-  ``/users/clinician_user/(?P<user_id>\d+)/is_provider/``

-  ``/users/clinician_user/(?P<user_id>\d+)/set_provider/``

-  ``/users/clinician_user/search/``

-  ``/users/clinician_user/(?P<user_id>\d+)/get_user_info/``

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/clinician_user/(?P<user_id>\d+)/is_provider/

    from helper import *

    USER_ID = 2

    clinician_user = compose_get_request('/users/clinician_user/%d/is_provider/' % USER_ID)
    print_response_data('clinician_user', clinician_user)

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/clinician_user/(?P<user_id>\d+)/set_provider/

    from helper import *

    USER_ID = 2

    clinician_user = compose_post_request('/users/clinician_user/%d/set_provider/' % USER_ID)
    print_response_data('clinician_user', clinician_user)

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/clinician_user/search/

    from helper import *

    QUERY_STRING = 'test'

    clinician_user = compose_get_request('/users/clinician_user/search/', QUERY_STRING)
    print_response_data('clinician_user', clinician_user)

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/clinician_user/(?P<user_id>\d+)/get_user_info/

    from helper import *

    USER_ID = 2

    clinician_user = compose_get_request('/users/clinician_user/%d/get_user_info/' % USER_ID)
    print_response_data('clinician_user', clinician_user)


Now you can run logout API:

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/user/logout/

    from helper import *

    response_content = compose_get_request('/users/user/logout/')
    print_response_data('user', response_content)
