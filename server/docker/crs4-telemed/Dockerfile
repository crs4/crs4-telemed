FROM ubuntu:14.04.4

RUN apt-get update && apt-get install -y asterisk wget vim mysql-server libmysqlclient-dev git odbcinst python-pip build-essential sqlite3 \
	unixodbc unixodbc-dev
RUN pip install Django==1.7.9 shortuuid pytz hl7apy requests==2.13.0

WORKDIR /opt

RUN wget http://www.creytiv.com/pub/re-0.4.15.tar.gz && tar zxf re-0.4.15.tar.gz
RUN wget http://www.creytiv.com/pub/restund-0.4.12.tar.gz && tar zxf restund-0.4.12.tar.gz

RUN cd re-0.4.15 && make && make install && ln -s /usr/local/lib/libre.so /usr/lib/libre.so
RUN cd restund-0.4.12 && make && make install
RUN rm -f /etc/asterisk/extconfig.conf && ln -s /opt/crs4-telemed/server/docker/crs4-telemed/extconfig.conf /etc/asterisk/extconfig.conf
RUN rm -f /etc/asterisk/extensions.conf && ln -s /opt/crs4-telemed/server/docker/crs4-telemed/extensions.conf /etc/asterisk/extensions.conf
RUN rm -f /etc/asterisk/func_odbc.conf && ln -s /opt/crs4-telemed/server/docker/crs4-telemed/func_odbc.conf /etc/asterisk/func_odbc.conf
RUN rm -f /etc/asterisk/modules.conf && ln -s /opt/crs4-telemed/server/docker/crs4-telemed/modules.conf /etc/asterisk/modules.conf
RUN rm -f /etc/asterisk/res_odbc.conf && ln -s /opt/crs4-telemed/server/docker/crs4-telemed/res_odbc.conf /etc/asterisk/res_odbc.conf
RUN rm -f /etc/asterisk/sip.conf && ln -s /opt/crs4-telemed/server/docker/crs4-telemed/sip.conf /etc/asterisk/sip.conf
RUN rm -f /etc/odbcinst.ini && ln -s /opt/crs4-telemed/server/docker/crs4-telemed/odbcinst.ini /etc/odbcinst.ini
RUN rm -f /etc/odbc.ini && ln -s /opt/crs4-telemed/server/docker/crs4-telemed/odbc.ini /etc/odbc.ini
RUN rm -f /etc/restund.conf && ln -s /opt/crs4-telemed/server/docker/crs4-telemed/restund.conf /etc/restund.conf
COPY asterisk_db.sql /opt/
COPY restund_db.sql /opt/
RUN ln -s /opt/crs4-telemed/server/docker/crs4-telemed/run.sh /usr/local/bin/run.sh
RUN ln -s /opt/crs4-telemed/server/docker/crs4-telemed/init_django.sh /usr/local/bin/init_django.sh

RUN service mysql start && \
	mysql -u root -e "CREATE USER 'most'@'localhost' IDENTIFIED BY 'most'" && \
	mysql -u root < asterisk_db.sql && \
	mysql -u root < restund_db.sql && \
	mysql -u root -e "GRANT ALL PRIVILEGES ON most.* TO 'most'@'localhost'" && \
	mysql -u root -e "GRANT ALL PRIVILEGES ON turnauth.* TO 'most'@'localhost'" && \
	mysql -u root -e "SET PASSWORD FOR 'root'@'localhost' = PASSWORD('most')" && \
	service mysql stop

VOLUME ["/opt/crs4-telemed"]

WORKDIR /opt/crs4-telemed

EXPOSE 2000 3306 3478 5060 8000 10000-10010
