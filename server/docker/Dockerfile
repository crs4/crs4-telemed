FROM ubuntu:14.04.4

RUN apt-get update && apt-get install -y asterisk wget vim mysql-server libmysqlclient-dev git odbcinst python-pip build-essential sqlite3 \
	unixodbc unixodbc-dev
RUN pip install Django==1.7.9 shortuuid pytz

WORKDIR /tmp

RUN wget http://www.creytiv.com/pub/re-0.4.15.tar.gz && tar zxf re-0.4.15.tar.gz
RUN wget http://www.creytiv.com/pub/restund-0.4.12.tar.gz && tar zxf restund-0.4.12.tar.gz

RUN cd re-0.4.15 && make && make install && ln -s /usr/local/lib/libre.so /usr/lib/libre.so
RUN cd restund-0.4.12 && make && make install
COPY extconfig.conf /etc/asterisk/
COPY extensions.conf /etc/asterisk/
COPY func_odbc.conf /etc/asterisk/
COPY modules.conf /etc/asterisk/
COPY res_odbc.conf /etc/asterisk/
COPY sip.conf /etc/asterisk/
COPY odbcinst.ini /etc/odbcinst.ini
COPY odbc.ini /etc/odbc.ini
COPY restund.conf /etc/restund.conf
COPY asterisk_db.sql /tmp/
COPY restund_db.sql /tmp/
COPY run.sh /usr/local/bin/
COPY init_django.sh /usr/local/bin/
COPY django_initial.json /tmp/
RUN chmod +x /usr/local/bin/run.sh
RUN chmod +x /usr/local/bin/init_django.sh

RUN service mysql start && \
	mysql -u root -e "CREATE USER 'most'@'localhost' IDENTIFIED BY 'most'" && \
	mysql -u root < asterisk_db.sql && \
	mysql -u root < restund_db.sql && \
	mysql -u root -e "GRANT ALL PRIVILEGES ON most.* TO 'most'@'localhost'" && \
	mysql -u root -e "GRANT ALL PRIVILEGES ON turnauth.* TO 'most'@'localhost'" && \
	mysql -u root -e "SET PASSWORD FOR 'root'@'localhost' = PASSWORD('most')" && \
	service mysql stop

VOLUME ["/tmp/most-demo"]

EXPOSE 2000 3306 3478 5060 8000 10000-10010
