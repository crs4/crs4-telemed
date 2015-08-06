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


MostUser module provides the following **REST API** (run login api before run the following):

-  ``/users/user/new/``

-  ``/users/user/(?P<user_id>\d+)/get_user_info/``

-  ``/users/user/search/``

-  ``/users/user/(?P<user_id>\d+)/edit/``

-  ``/users/user/(?P<user_id>\d+)/deactivate/``

-  ``/users/user/(?P<user_id>\d+)/activate/``

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/user/new/

    from helper import *


    USER_DATA = {
        'username': 'mario.rossi',
        'first_name': 'Mario',
        'last_name': 'Rossi',
        'email': 'mario.rossi@most.crs4.it',
        'birth_date': '1980-07-08',
        'is_active': True,
        'is_admin': False,
        'numeric_password': 1234,
        'user_type': 'CL',
        'gender': 'M',
        'phone': '070789456',
        'mobile': '888987654',
    }

    user = compose_post_request('/users/user/new/', USER_DATA)
    print_response_data('user', user)

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/user/(?P<user_id>\d+)/get_user_info/

    from helper import *

    USER_ID = 1

    user = compose_get_request('/users/user/%d/get_user_info/' % USER_ID)
    print_response_data('user', user)

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/user/search/

    from helper import *

    QUERY_STRING = 'test'

    users = compose_get_request('/users/user/search/', QUERY_STRING)
    print_response_data('user', users)

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/user/(?P<user_id>\d+)/edit/

    from helper import *

    USER_ID = 1
    USER_DATA = {
        'username': 'valeria',
        'first_name': 'Valeria',
        'last_name': 'Lecca',
        'email': 'valeria.lecca@most.crs4.it',
        'birth_date': '1980-06-11',
        'is_active': True,
        'is_admin': True,
        'numeric_password': 1234,
        'user_type': 'TE',
        'gender': 'F',
        'phone': '070789456',
        'mobile': '888987654',
    }

    user = compose_post_request('/users/user/%d/edit/' % USER_ID, USER_DATA)
    print_response_data('user', user)

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/user/(?P<user_id>\d+)/deactivate/

    from helper import *

    USER_ID = 9

    user = compose_get_request('/users/user/%d/deactivate/' % USER_ID)
    print_response_data('user', user)

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/user/(?P<user_id>\d+)/activate/

    from helper import *

    USER_ID = 9

    user = compose_post_request('/users/user/%d/activate/' % USER_ID)
    print_response_data('user', user)


Now you can run logout API:

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/user/logout/

    from helper import *

    response_content = compose_get_request('/users/user/logout/')
    print_response_data('user', response_content)
