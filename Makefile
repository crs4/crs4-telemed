devel:
	echo "clone libs"

	@if ! [ -d libs/most ]; then git clone https://github.com/crs4/most libs/most -b develop; fi
	@if ! [ -d libs/most-streaming ]; then git clone https://github.com/crs4/most-streaming libs/most-streaming -b develop; fi
	@if ! [ -d libs/most-voip ]; then git clone https://github.com/crs4/most-voip libs/most-voip -b develop; fi
	@if ! [ -d libs/most-visualization ]; then git clone https://github.com/crs4/most-visualization libs/most-visualization -b develop; fi

	echo "link libs"

	cd server/most/web ; ln -s ../../../libs/most/src/most/web/utils utils; ln -s ../../../libs/most/src/most/web/users users ; \
	ln -s ../../../libs/most/src/most/web/authentication authentication; \
	ln -s ../../../libs/most-streaming/service/src/most/web/streaming streaming; \
	ln -s ../../../libs/most-voip/service/src/most/web/voip voip; \

	cd server; ln -fs ../libs/most/src/provider provider;

clean:
	echo "clean devel mode"
	
	rm server/provider

	@if [ `git -C libs/most status --porcelain` ]]; then \
		echo "CHANGES - most repository not removed"; \
	else \
		echo "NO CHANGES - remove most repository"; \
		rm -fr libs/most; \
		rm -f server/most/web/utils; \
		rm -f server/most/web/users; \
		rm -f server/most/web/authentication; \
	fi

	@if [ `git -C libs/most-streaming status --porcelain` ]]; then \
		echo "CHANGES - most streaming repository not removed"; \
	else \
		echo "NO CHANGES - remove most streaming repository"; \
		rm -fr libs/most-streaming; \
		rm -f server/most/web/streaming; \
	fi

	@if [ `git -C libs/most-voip status --porcelain` ]; then \
		echo "CHANGES - most voip repository not removed"; \
	else \
		echo "NO CHANGES - remove most voip repository"; \
		rm -fr libs/most-voip; \
		rm -f server/most/web/voip; \
	fi


	@if [ `git -C libs/most-visualization status --porcelain` ]; then \
		echo "CHANGES - most visualization repository not removed"; \
	else \
		echo "NO CHANGES - remove most visualization repository"; \
		rm -fr libs/most-visualization; \
	fi

run:
	cd server/most; PYTHONPATH=.. python manage.py runserver 0.0.0.0:8000

shell: 
	cd server/most; PYTHONPATH=.. python manage.py shell

sync:
	cd server/most; PYTHONPATH=.. python manage.py makemigrations
	cd server/most; PYTHONPATH=.. python manage.py migrate

dump:
	cd server/most; PYTHONPATH=.. python manage.py dumpdata --exclude contenttypes --exclude auth --exclude sessions --exclude admin --natural-foreign

test:
	cd src/most/web/medicalrecords/; nosetests --logging-level=DEBUG -s
