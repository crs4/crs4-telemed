APIs
~~~~

   .. http:method:: POST /users/user/new/

      Create new user.

      :parameter str username: the user nickname. Max length 30
      :parameter str first_name: the user first name. Max length 50
      :parameter str last_name: the user last name. Max length 50
      :parameter str email: the user email. Max length 250
      :parameter datetime birth_date: the user birth date
      :parameter boolean is_staff: True if the user is staff member, False otherwise. Default to True
      :parameter boolean is_active: True if the user is active, False otherwise. Default to True
      :parameter boolean is_admin: True if the user has administrator privileges. Default to False
      :parameter int numeric_password: the user pin. String of 4 numbers
      :parameter str user_type: the user type: 'AD' for Administrative, 'TE' for Technician, 'CL' for Clinician, 'ST' for Student
      :parameter str gender: the user gender: 'M' for Male, 'F' for Female, 'U' for Unknown
      :parameter str phone: the user phone number. Max length 20
      :parameter str mobile: the user mobile phone number. Max length 20
      :parameter str certified_email: the user legal mail. Max length 255
      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if the user is successfully created. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains the created user data in json format


   .. http:method:: POST /users/user/login/

      Log a user in the system

      :parameter str username: the user nickname
      :parameter str password: the user password
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if the user is successfully logged in. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains the logged user data in json format


   .. http:method:: GET /users/user/logout/

      Log a user out of the system

      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if the user is successfully logged out. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems


   .. http:method:: GET /users/user/(user_id)/get_user_info/

      Get the information of the user identified by `user_id`

      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if the user is successfully found. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains the data of user identified by `user_id`, in json format


   .. http:method:: GET /users/user/search/

      Get a list of users matching a query string in fields: username, last_name, first_name, email or certified_email

      :parameter str query_string: the query string to search

      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if users matching the query string are found. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains the a list of
            data of users matching the query string, in json format


   .. http:method:: POST /users/user/(user_id)/edit/

      Edit the information of the user identified by `user_id`

      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if the user is successfully found and updated. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains the updated data of user identified by `user_id`, in json format


   .. http:method:: POST /users/user/(user_id)/deactivate/

      Deactivate the user identified by `user_id`

      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if the user is successfully deactivated. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains the keys `id` (for the user id) and `is_active` (for the activation state):


   .. http:method:: POST /users/user/(user_id)/activate/

      Activate the user identified by `user_id`

      :requestheader Authorization: login required
      :responseheader Content-Type: application/json

         :parameter boolean `success`: True if the user is successfully activated. False otherwise
         :parameter str `message`: a feedback string that would be displayed to the connected user
         :parameter str `errors`: an error string that explains the raised problems
         :parameter json `data`: if success is True, it contains the keys `id` (for the user id) and `is_active` (for the activation state)

