APIs
~~~~

   .. http:method:: POST /users/task_group/new/

      Create new task group

      :parameter str title: the task group title. Max length 100
      :parameter str description: the task group description. Max length 100
      :parameter str task_group_type: the task group type: 'SP' for Service Provider and 'HF' for Health Care Facilities
      :parameter str hospital: the task group hospital. Max length 100
      :parameter array users: the list of users that belong to task group
      :parameter boolean is_health_care_provider: True if the task group is health care provider, False otherwise.
      :parameter boolean is_active: True if the user is active, False otherwise.
      :parameter array related_task_groups: the list of task groups that may benefit of health care services
      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if the task group is successfully created. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains the created task group data in json format


   .. http:method:: GET /users/task_group/search/

      Get a list of task group matching a query string in fields: title, description or hospital

      :parameter str query_string: the query string to search

      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if task groups matching the query string are found. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains the a list of
            data of task groups matching the query string, in json format


   .. http:method:: POST /users/task_group/(task_group_id)/edit/

      Edit the information of the task group identified by `task_group_id`

      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if the task group is successfully found and updated. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains the updated data of task group identified by `task_group_id`, in json format



   .. http:method:: GET /users/task_group/list_available_states/

      Get a list of available state of activation

      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if states are successfully found. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains an array of available activation states in json format


   .. http:method:: POST /users/task_group/(task_group_id)/set_active_state/(active_state)/

      Set the activation state `active_state` (active or inactive) to the task group identified by `task_group_id`

      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if task group is found and updated. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains the keys `id` (for the task group id) and `is_active` (for the activation state), in json format


   .. http:method:: GET /users/task_group/(task_group_id)/is_provider/

      Investigate if the task group identified by `task_group_id` is health care provider

      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if the task group is successfully found. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains the keys `id` (for the task group id) and `is_health_care_provider` (for the health care provider state)


   .. http:method:: POST /users/task_group/(task_group_id)/set_provider/

      Set the task group identified by `task_group_id` as health care provider

      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if task group is found and updated. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains the keys `id` (for the task group id) and `is_active` (for the activation state), in json format


   .. http:method:: POST /users/task_group/(task_group_id)/add_user/(user_id)/

      Add the user identified by `user_id` to the task group identified by `task_group_id`

      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if task group is updated. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains the keys `task_group_id` (for the task group id) and `user_id` (for the user just added to the task group), in json format


   .. http:method:: POST /users/task_group/(task_group_id)/remove_user/(user_id)/

      Remove the user identified by `user_id` from the task group identified by `task_group_id`

      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if task group is updated. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains the keys `task_group_id` (for the task group id) and `user_id` (for the user just removed from the task group), in json format


   .. http:method:: GET /users/task_group/(task_group_id)/list_users/

      List all users that belong to the task group identified by `task_group_id`

      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if task group is found. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains an array of data of users that belong to the task group, in json format


   .. http:method:: POST /users/task_group/(task_group_id)/add_related_task_group/(related_task_group_id)/

      Add the related task group identified by `related_task_group_id` to the task group identified by `task_group_id`

      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if task group is updated. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains the keys `task_group_id` (for the task group id) and `related_task_group_id` (for the related task group just added to the task group), in json format


   .. http:method:: POST /users/task_group/(task_group_id)/remove_related_task_group/(related_task_group_id)/

      Remove the related task group identified by `related_task_group_id` from the task group identified by `task_group_id`

      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if task group is updated. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains the keys `task_group_id` (for the task group id) and `related_task_group_id` (for the related task group just removed from the task group), in json format


   .. http:method:: GET /users/task_group/(task_group_id)/list_related_task_groups/

      List all related task groups that belong to the task group identified by `task_group_id`

      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if task group is found. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains an array of data of related task groups that belong to the task group, in json format


   .. http:method:: GET /users/task_group/(task_group_id)/has_clinicians/

      Investigate if the task group identified by `task_group_id` has clinician users

      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if clinician users are successfully found. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains the keys `task_group_id` (for the task group id) and `clinicians_count` (for the number of clinician user that belong to task group)


   .. http:method:: GET /users/task_group/(task_group_id)/list_clinicians/

      List all related clinician users that belong to the task group identified by `task_group_id`

      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if task group is found. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains an array of data of clinician users that belong to the task group, in json format


   .. http:method:: GET /users/task_group/(task_group_id)/has_clinician_provider/

      Investigate if the task group identified by `task_group_id` has health care provider clinician users

      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if health care providers are successfully found. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains the keys `task_group_id` (for the task group id) and `clinicians_count` (for the number of health care provider clinician user that belong to task group)


   .. http:method:: GET /users/task_group/(task_group_id)/list_clinician_providers/

      List all health care provider clinician users that belong to the task group identified by `task_group_id`

      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if task group is found. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains an array of data of health care provider clinician users that belong to the task group, in json format
