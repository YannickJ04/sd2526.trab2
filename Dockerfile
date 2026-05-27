FROM nunopreguica/sd2526tpbase

WORKDIR /home/sd

ADD hibernate.cfg.xml .
ADD messages.props .

ADD truststore.ks .
ADD users.ourorg0.ks .
ADD users.ourorg1.ks .
ADD users.ourorg2.ks .
ADD messages0.ourorg0.ks .
ADD messages1.ourorg0.ks .
ADD messages2.ourorg0.ks .
ADD messages0.ourorg1.ks .
ADD messages1.ourorg1.ks .
ADD messages2.ourorg1.ks .
ADD messages.ourorg2.ks .

COPY target/sd2526-tp2-ref-1.jar sd2526.jar