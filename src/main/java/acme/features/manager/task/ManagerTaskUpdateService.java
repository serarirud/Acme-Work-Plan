package acme.features.manager.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.entities.tasks.Task;
import acme.framework.components.Errors;
import acme.framework.components.Model;
import acme.framework.components.Request;
import acme.framework.entities.Manager;
import acme.framework.entities.Principal;
import acme.framework.services.AbstractUpdateService;

@Service
public class ManagerTaskUpdateService implements AbstractUpdateService<Manager, Task>{

	
	@Autowired
	protected ManagerTaskRepository repository;
	
	@Override
	public boolean authorise(Request<Task> request) {
		assert request != null;
		
		int taskId;
		Task task;
		Manager manager;
		Principal principal;
		
		taskId = request.getModel().getInteger("id");
		task = this.repository.findOneById(taskId);
		manager = task.getManager();
		principal = request.getPrincipal();
		
		return manager.getUserAccount().getId() == principal.getAccountId();
	}

	@Override
	public void bind(Request<Task> request, Task entity, Errors errors) {
		assert request != null;
		assert entity != null;
		assert errors != null;
		
		request.bind(entity, errors);
	}

	@Override
	public void unbind(Request<Task> request, Task entity, Model model) {
		assert request != null;
		assert entity != null;
		assert model != null;
		
		request.unbind(entity, model, "title", "startExecutionPeriod", "endExecutionPeriod");
		request.unbind(entity, model, "workload", "description", "link", "isPublic");	
	}

	@Override
	public Task findOne(Request<Task> request) {
		assert request != null;
		
		int id = request.getModel().getInteger("id");
		return this.repository.findOneById(id);
	}

	@Override
	public void validate(Request<Task> request, Task entity, Errors errors) {
		assert request != null;
		assert entity != null;
		assert errors != null;
		
		if(!errors.hasErrors("endExecutionPeriod")) {
			errors.state(request, entity.getEndExecutionPeriod().after(entity.getStartExecutionPeriod()), "endExecutionPeriod", "manager.task.form.error.end");
		}
		
		if(!errors.hasErrors("workload")) {
			Double workload = entity.getWorkload();
			String str = String.valueOf(workload);
			
			int decNumberInt = Integer.parseInt(str.substring(str.indexOf(".")+1));
			errors.state(request, decNumberInt<60, "workload", "manager.task.form.error.workload");
		}
		
	}

	@Override
	public void update(Request<Task> request, Task entity) {
		assert request != null;
		assert entity != null;
		
		this.repository.save(entity);
		
	}

}