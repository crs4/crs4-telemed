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

TaskGroup module provides the following **REST API** (run login api before run the following):

-  ``/users/task_group/search/``

-  ``/users/task_group/new/``

-  ``/users/task_group/(?P<task_group_id>\d+)/get_task_group_info/``

-  ``/users/task_group/(?P<task_group_id>\d+)/edit/``

-  ``/users/task_group/list_available_states/``

-  ``/users/task_group/(?P<task_group_id>\d+)/set_active_state/(?P<active_state>\w+)/``

-  ``/users/task_group/(?P<task_group_id>\d+)/is_provider/``

-  ``/users/task_group/(?P<task_group_id>\d+)/set_provider/``

-  ``/users/task_group/(?P<task_group_id>\d+)/add_user/(?P<user_id>\d+)/``

-  ``/users/task_group/(?P<task_group_id>\d+)/remove_user/(?P<user_id>\d+)/``

-  ``/users/task_group/(?P<task_group_id>\d+)/list_users/``

-  ``/users/task_group/(?P<task_group_id>\d+)/add_related_task_group/(?P<related_task_group_id>\d+)/``

-  ``/users/task_group/(?P<task_group_id>\d+)/remove_related_task_group/(?P<related_task_group_id>\d+)/``

-  ``/users/task_group/(?P<task_group_id>\d+)/list_related_task_groups/``

-  ``/users/task_group/(?P<task_group_id>\d+)/has_clinicians/``

-  ``/users/task_group/(?P<task_group_id>\d+)/list_clinicians/``

-  ``/users/task_group/(?P<task_group_id>\d+)/has_clinician_provider/``

-  ``/users/task_group/(?P<task_group_id>\d+)/list_clinician_providers/``

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/task_group/search/

    from helper import *

    QUERY_STRING = 'CRS'

    task_groups = compose_get_request('/users/task_group/search/', QUERY_STRING)
    print_response_data('task_group', task_groups)

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/task_group/new/

    from helper import *

    TASK_GROUP_DATA = {
        'title': 'Notebook group 7',
        'description': 'Example task group, create by notebook user interface',
        'task_group_type': 'HF',
        'is_active': True,
        'is_health_care_provider': True,
    }

    task_group = compose_post_request('/users/task_group/new/', TASK_GROUP_DATA)
    print_response_data('task_group', task_group)

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/task_group/(?P<task_group_id>\d+)/get_task_group_info/

    from helper import *

    TASK_GROUP_ID = 2

    task_group = compose_get_request('/users/task_group/%d/get_task_group_info/' % TASK_GROUP_ID)
    print_response_data('task_group', task_group)

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/task_group/(?P<task_group_id>\d+)/edit/

    from helper import *

    TASK_GROUP_ID = 2
    TASK_GROUP_DATA = {
        "description": "Generici",
        "hospital": "Clinica d'esempio",
        "id": "2",
        "is_active": True,
        "is_health_care_provider": False,
        "task_group_type": "HF",
        "title": "Clinica"
    }

    task_group = compose_post_request('/users/task_group/%d/edit/' % TASK_GROUP_ID, TASK_GROUP_DATA)
    print_response_data('task_group', task_group)

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/task_group/list_available_states/

    from helper import *

    task_group = compose_get_request('/users/task_group/list_available_states/')
    print_response_data('task_group', task_group)

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/task_group/(?P<task_group_id>\d+)/set_active_state/(?P<active_state>\w+)/

    from helper import *

    TASK_GROUP_ID = 2
    TASK_GROUP_ACTIVATION_STATE = 'inactive'

    task_group = compose_post_request('/users/task_group/%d/set_active_state/%s/' % (TASK_GROUP_ID, TASK_GROUP_ACTIVATION_STATE))
    print_response_data('task_group', task_group)

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/task_group/(?P<task_group_id>\d+)/is_provider/

    from helper import *

    TASK_GROUP_ID = 2

    task_group = compose_get_request('/users/task_group/%d/is_provider/' % TASK_GROUP_ID)
    print_response_data('task_group', task_group)

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/task_group/(?P<task_group_id>\d+)/set_provider/

    from helper import *

    TASK_GROUP_ID = 2

    task_group = compose_post_request('/users/task_group/%d/set_provider/' % TASK_GROUP_ID)
    print_response_data('task_group', task_group)

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/task_group/(?P<task_group_id>\d+)/add_user/(?P<user_id>\d+)/

    from helper import *

    TASK_GROUP_ID = 2
    USER_ID = 3

    task_group = compose_post_request('/users/task_group/%d/add_user/%d/' % (TASK_GROUP_ID, USER_ID))
    print_response_data('task_group', task_group)

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/task_group/(?P<task_group_id>\d+)/remove_user/(?P<user_id>\d+)/

    from helper import *

    TASK_GROUP_ID = 2
    USER_ID = 3

    task_group = compose_post_request('/users/task_group/%d/remove_user/%d/' % (TASK_GROUP_ID, USER_ID))
    print_response_data('task_group', task_group)

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/task_group/(?P<task_group_id>\d+)/list_users/

    from helper import *

    TASK_GROUP_ID = 2

    task_group = compose_get_request('/users/task_group/%d/list_users/' % TASK_GROUP_ID)
    print_response_data('task_group', task_group)

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/task_group/(?P<task_group_id>\d+)/add_related_task_group/(?P<related_task_group_id>\d+)/

    from helper import *

    TASK_GROUP_ID = 2
    RELATED_TASK_GROUP_ID = 26

    task_group = compose_post_request('/users/task_group/%d/add_related_task_group/%d/' % (TASK_GROUP_ID, RELATED_TASK_GROUP_ID))
    print_response_data('task_group', task_group)

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/task_group/(?P<task_group_id>\d+)/remove_related_task_group/(?P<related_task_group_id>\d+)/

    from helper import *

    TASK_GROUP_ID = 2
    RELATED_TASK_GROUP_ID = 26

    task_group = compose_post_request('/users/task_group/%d/remove_related_task_group/%d/' % (TASK_GROUP_ID, RELATED_TASK_GROUP_ID))
    print_response_data('task_group', task_group)

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/task_group/(?P<task_group_id>\d+)/list_related_task_groups/

    from helper import *

    TASK_GROUP_ID = 2

    task_group = compose_get_request('/users/task_group/%d/list_related_task_groups/' % TASK_GROUP_ID)
    print_response_data('task_group', task_group)

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/task_group/(?P<task_group_id>\d+)/has_clinicians/

    from helper import *

    TASK_GROUP_ID = 2

    task_group = compose_get_request('/users/task_group/%d/has_clinicians/' % TASK_GROUP_ID)
    print_response_data('task_group', task_group)

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/task_group/(?P<task_group_id>\d+)/list_clinicians/

    from helper import *

    TASK_GROUP_ID = 2

    task_group = compose_get_request('/users/task_group/%d/list_clinicians/' % TASK_GROUP_ID)
    print_response_data('task_group', task_group)

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/task_group/(?P<task_group_id>\d+)/has_clinician_provider/

    from helper import *

    TASK_GROUP_ID = 2

    task_group = compose_get_request('/users/task_group/%d/has_clinician_provider/' % TASK_GROUP_ID)
    print_response_data('task_group', task_group)

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/task_group/(?P<task_group_id>\d+)/list_clinician_providers/

    from helper import *

    TASK_GROUP_ID = 2

    task_group = compose_get_request('/users/task_group/%d/list_clinician_providers/' % TASK_GROUP_ID)
    print_response_data('task_group', task_group)


Now you can run logout API:

.. code:: python

    # -*- coding: utf-8 -*-

    # API: /users/user/logout/

    from helper import *

    response_content = compose_get_request('/users/user/logout/')
    print_response_data('user', response_content)
