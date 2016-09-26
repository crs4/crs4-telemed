from django.conf.urls import patterns, include, url

# Uncomment the next two lines to enable the admin:
from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns(
    '',
    url(r'^authentication/', include('most.web.authentication.urls')),
    # url(r'^authentication/test', 'most.web.authentication.views.test_auth'),
    url(r'^teleconsultation/', include('most.web.teleconsultation.urls')),

    # Uncomment the admin/doc line below to enable admin documentation:
    url(r'^admin/doc/', include('django.contrib.admindocs.urls')),

    # Uncomment the next line to enable the admin:
    url(r'^admin/', include(admin.site.urls)),

    # Oauth2 urls
    url(r'^oauth2/', include('provider.oauth2.urls', namespace='oauth2')),
)
