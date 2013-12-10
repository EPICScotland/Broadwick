#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )

package ${package};

import broadwick.model.Model;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class App extends Model {

    @Override
    public final void init() {
	log.info("Initialising project");
    }

    @Override
    public final void run() {
	log.info("Running project");
    }

    @Override
    public final void finalise() {
	log.info("Closing project");
    }

}

