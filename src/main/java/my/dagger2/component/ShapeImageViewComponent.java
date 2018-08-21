package my.dagger2.component;

import dagger.Component;
import my.custom.view.ShapeImageView;
import my.dagger2.module.ShapeImageViewModule;

/**
 * @author djh on  2018/8/21 11:49
 * @E-Mail 1544579459@qq.com
 */
@Component(modules = ShapeImageViewModule.class)
public interface ShapeImageViewComponent {

    /**
     * Use to inject rely to this custom view.
     *
     * @param shapeImageView aim inject host.
     */
    void inject(ShapeImageView shapeImageView);
}
