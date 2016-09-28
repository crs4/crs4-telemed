from django.conf.urls import patterns, include, url

# Uncomment the next two lines to enable the admin:
from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns(
    '',
    url(r'^admin/', include(admin.site.urls)),
    url(r'^authentication/', include('most.web.authentication.urls')),
    url(r'^teleconsultation/', include('most.web.teleconsultation.urls')),
    url(r'^demographics/', include('most.web.demographics.urls')),
    url(r'^oauth2/', include('provider.oauth2.urls', namespace='oauth2')),
)
