APIs
~~~~

   .. http:method:: POST /users/clinician_user/new/

      Create new clinician user.

      :parameter int user: the user id of the related user
      :parameter str clinician_type: the clinician user type: 'DR' for Doctor or 'OP' for Operator
      :parameter str specialization: the clinician user specialization. Max length 50
      :parameter boolean is_health_care_provider: True if the clinician user is health care provider, False otherwise
      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if the clinician user is successfully created. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains the created user data in json format


   .. http:method:: GET /users/clinician_user/(user_id)/is_provider/

      Investigate if the clinician user, with related user identified by `user_id`, is health care provider

      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if the clinician user is successfully found. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains the keys `user_id` (for the related user id) and `is_health_care_provider` (for the health care provider state)


   .. http:method:: POST /users/clinician_user/(user_id)/set_provider/

      Set the clinician user, with related user identified by `user_id`, health care provider state to True

      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if the clinician user is successfully updated. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains the keys `user_id` (for the related user id) and `is_health_care_provider` (for the health care provider state)


   .. http:method:: GET /users/clinician_user/search/

      Get a list of clinician users matching a query string in fields: username, last_name, first_name, email, certified_email or specialization

      :parameter str query_string: the query string to search

      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if clinician users matching the query string are found. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains the a list of
            data of clinician users matching the query string, in json format


   .. http:method:: GET /users/clinician_user/(user_id)/get_user_info/

      Get the information of the clinician user, with related user identified by `user_id`

      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if the clinician user is successfully found. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains the data of clinician user, with related user identified by `user_id`, in json format
