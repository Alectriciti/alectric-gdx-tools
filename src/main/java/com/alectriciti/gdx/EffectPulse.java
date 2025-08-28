package com.alectriciti.gdx;

import static com.alectriciti.gdx.Toolkit.*;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;

public class EffectPulse extends Widget{
	
	
	
	public EffectPulse(Widget w, Rectangle r, Color c) {
		super(w.manager);
		this.render_text = false;
		this.shape = new Rectangle(r);
		this.color = c.cpy();
	}
	
	public float effect_offset_start = 2;
	public static float effect_move_speed = 0.3333f;
	
	float thickness = 7;
	float thickness_target = 1;
	float thickness_lerp_a = 0.95f;
	
	float effect_alpha = 1f;
	float effect_size_delta = 0;
	
	
	@Override
	protected void update() {
		// TODO Auto-generated method stub
		
		super.update();
		effect_alpha *= 0.92f;
		thickness = lerp(thickness_target, thickness, thickness_lerp_a);
		effect_size_delta += effect_move_speed;
		if(effect_alpha<0.01) {
			destroy();
		}
	}
	
	
	@Override
	public void drawShape(ShapeRenderer renderer, boolean recursive) {
		// TODO Auto-generated method stub
		drawButtonEffect(renderer);
		super.drawShape(renderer, recursive);
	}

	
	protected void drawButtonEffect(ShapeRenderer renderer) {
		color.a = effect_alpha;
		renderer.set(ShapeType.Line);
		renderer.setColor(color);
		//renderer.line
		
		
		renderer.rect(shape.x - effect_size_delta - effect_offset_start,
				shape.y - effect_size_delta - effect_offset_start,
				shape.width + ((effect_size_delta + effect_offset_start) * 2),
				shape.height + ((effect_size_delta + effect_offset_start) * 2));
		// renderer.rect(getGlobalX()-(effect_delta/2)-effect_offset_start,
		// getGlobalY()-(effect_delta/2)-effect_offset_start,
		// shape.width+effect_delta+effect_offset_start,
		// shape.height+effect_delta+effect_offset_start);

	}


}
